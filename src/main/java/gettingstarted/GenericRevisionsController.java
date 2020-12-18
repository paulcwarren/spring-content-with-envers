package gettingstarted;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://github.com/spring-projects/spring-data-envers/issues/35

@RepositoryRestController
public class GenericRevisionsController implements InitializingBean {

    private static Method REVISIONS_METHOD = null;
    private static Method FIND_REVISION_METHOD = null;
    private static Method FIND_LAST_CHANGE_REVISION_METHOD = null;

    static {
        REVISIONS_METHOD = ReflectionUtils.findMethod(RevisionRepository.class, "findRevisions", Object.class, Pageable.class);
        Assert.notNull(REVISIONS_METHOD, "findRevisions method cannot be null");
        FIND_REVISION_METHOD = ReflectionUtils.findMethod(RevisionRepository.class, "findRevision", Object.class, Number.class);
        Assert.notNull(FIND_REVISION_METHOD, "findRevision method cannot be null");
        FIND_LAST_CHANGE_REVISION_METHOD = ReflectionUtils.findMethod(RevisionRepository.class, "findLastChangeRevision", Object.class);
        Assert.notNull(FIND_LAST_CHANGE_REVISION_METHOD, "findLastChangeRevision method cannot be null");
    }

    private final Repositories repositories;
    private final PagedResourcesAssembler<Object> pagedResourcesAssembler;

    @Autowired
    private ObjectMapper objectMapper;

    GenericRevisionsController(Repositories repositories, PagedResourcesAssembler<Object> assembler) {
        this.repositories = repositories;
        this.pagedResourcesAssembler = assembler;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{repository}/{id}/revisions", method = RequestMethod.GET, produces="application/hal+json")
    public ResponseEntity<CollectionModel<?>> getRevisions(
            RootResourceInformation rootResourceInformation,
            @PathVariable String repository,
            @PathVariable Long id,
            Pageable pageable,
            PersistentEntityResourceAssembler assembler) {

        Optional<Object> repo = repositories.getRepositoryFor(rootResourceInformation.getDomainType());
        repo.orElseThrow(() -> new IllegalStateException(format("Unable to find repository '%s'", repository)));

        Page<Object> page = (Page<Object>) ReflectionUtils.invokeMethod(REVISIONS_METHOD, repo.get(), id, pageable);

        if (page.getContent().isEmpty()) {

            return new ResponseEntity<CollectionModel<?>>(pagedResourcesAssembler.toEmptyModel(page, Revision.class), HttpStatus.OK);
        } else {

            List<Object> entities = new ArrayList<>();
            for (Object revision : page) {
                entities.add(((Revision)revision).getEntity());
            }

            return new ResponseEntity<CollectionModel<?>>(pagedResourcesAssembler.toModel(page), HttpStatus.OK);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{repository}/{id}/revisions/{revisionId}", method = RequestMethod.GET, produces="application/hal+json")
    public ResponseEntity<EntityModel<?>> getRevision(
            RootResourceInformation rootResourceInformation,
            @PathVariable String repository,
            @PathVariable Long id,
            @PathVariable Integer revisionId) {

        Optional<Object> repo = repositories.getRepositoryFor(rootResourceInformation.getDomainType());
        repo.orElseThrow(() -> new IllegalStateException(format("Unable to find repository '%s'", repository)));

        Optional<Revision<?,?>> revision = (Optional<Revision<?, ?>>) ReflectionUtils.invokeMethod(FIND_REVISION_METHOD, repo.get(), id, revisionId);

        if (revision.isPresent()) {
            return new ResponseEntity<EntityModel<?>>(EntityModel.of(revision.get()), HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{repository}/{id}/latestRevision", method = RequestMethod.GET, produces="application/hal+json")
    public ResponseEntity<EntityModel<?>> getLastChangeRevision(
            RootResourceInformation rootResourceInformation,
            @PathVariable String repository,
            @PathVariable Long id) {

        Optional<Object> repo = repositories.getRepositoryFor(rootResourceInformation.getDomainType());
        repo.orElseThrow(() -> new IllegalStateException(format("Unable to find repository '%s'", repository)));

        Optional<Revision<?,?>> revision = (Optional<Revision<?, ?>>) ReflectionUtils.invokeMethod(FIND_LAST_CHANGE_REVISION_METHOD, repo.get(), id);

        if (revision.isPresent()) {
            return new ResponseEntity<EntityModel<?>>(EntityModel.of(revision.get()), HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    public class RevisionOverride {
        @JsonIgnore RevisionMetadata metadata;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper.addMixIn(Revision.class, GenericRevisionsController.RevisionOverride.class);
    }
}
