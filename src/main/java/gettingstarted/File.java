package gettingstarted;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.content.commons.annotations.MimeType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Audited
@Entity
@Getter
@Setter
@NoArgsConstructor
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private Date created = new Date();
	private String summary;
	private String someNewAttribute;

	@ContentId
//	@GenericGenerator(strategy = AuditedGenerationStrategy.class)
	private String contentId;

	@ContentLength
	private long contentLength;

	@MimeType
	private String mimeType = "text/plain";
}
