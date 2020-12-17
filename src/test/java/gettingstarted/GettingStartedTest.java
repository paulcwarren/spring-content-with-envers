package gettingstarted;

import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.BeforeEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Context;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Describe;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.It;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.io.ByteArrayInputStream;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;

import com.github.paulcwarren.ginkgo4j.Ginkgo4jSpringRunner;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;

@RunWith(Ginkgo4jSpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GettingStartedTest {

    @Autowired
    private FileRepository fileRepo;
    @Autowired
    private FileContentStore fileContentStore;

    @Value("${local.server.port}")
    private int port;

    private File file;

    {
        Describe("Revision Tests", () -> {

            BeforeEach(() -> {
                RestAssured.port = port;
            });

            Context("given several revisions", () -> {

                BeforeEach(() -> {
                    File f = new File();
                    f.setName("test-file");
                    f.setMimeType("text/plain");
                    f.setSummary("test file summary");
                    f = fileContentStore.setContent(f, new ByteArrayInputStream("revision #1".getBytes()));
                    file = fileRepo.save(f);

                    f = fileContentStore.setContent(f, new ByteArrayInputStream("revision #2".getBytes()));
                    file = fileRepo.save(f);

                    f = fileContentStore.setContent(f, new ByteArrayInputStream("revision #3".getBytes()));
                    file = fileRepo.save(f);
                });

                It("should return them all", () -> {
                    Long fid = file.getId();

                    JsonPath jsonResponse = given().header("accept", "application/hal+json").when().get("/files/" + fid + "/revisions").then().statusCode(HttpStatus.SC_OK).extract().jsonPath();

                    assertThat(jsonResponse.getList("_embedded.revisions").size(), is(3));
                });

                It("should return each revision", () -> {
                    Long fid = file.getId();

                    Revisions<Integer, File> revisions = fileRepo.findRevisions(fid);
                    for (Revision<Integer, File> revision : revisions) {

                        JsonPath jsonResponse = given().header("accept", "application/hal+json").when().get("/files/" + fid + "/revisions/" + revision.getRevisionNumber().get()).then().statusCode(HttpStatus.SC_OK).extract().jsonPath();

                        assertThat(jsonResponse.get("entity.name"), is("test-file"));
                    }
                });

                It("should return the latest revision", () -> {
                    Long fid = file.getId();

                    JsonPath jsonResponse = given().header("accept", "application/hal+json").when().get("/files/" + fid + "/latestRevision").then().statusCode(HttpStatus.SC_OK).extract().jsonPath();

                    assertThat(jsonResponse.get("entity.name"), is("test-file"));
                });

                It("should return content for each revision", () -> {
                    Long fid = file.getId();

                    Revisions<Integer, File> revisions = fileRepo.findRevisions(fid);
                    for (Revision<Integer, File> revision : revisions) {

                        String response = given().header("accept", "text/plain").when().get("/files/" + fid + "/revisions/" + revision.getRevisionNumber().get() + "/content").then().statusCode(HttpStatus.SC_OK).extract().asString();

                        assertThat(response, matchesPattern("revision #\\d"));
                    }
                });
            });
        });
    }

    @Test
    public void noop() {
    }
}
