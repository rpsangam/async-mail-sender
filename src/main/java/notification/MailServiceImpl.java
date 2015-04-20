package notification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Response;

import notification.models.EmailMessage;
import notification.models.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class MailServiceImpl {

	private static final Logger logger = LoggerFactory
			.getLogger(Application.class);

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private RedisTemplate<String, EmailMessage> redisTemplate;

	@RequestMapping(method = RequestMethod.PUT, value = "trigger")
	public List<EmailMessage> triggerEmailJob(
			@RequestParam(value = "batchSize", required = false) int batchSize) {
		Query q = em.createQuery(
				" from EmailMessage em where em.status = 'NONE'",
				EmailMessage.class);
		List<EmailMessage> msgList = q.getResultList();

		// Ideally lock the operation, so other load balancing node won't pick
		// up same set of records.
		publishEmailMessage(msgList.get(0));
		updateEmailMsgStatus(Status.QUEUED, msgList);
		return msgList;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/testdata")
	@Transactional
	public Response createTestData(@RequestParam(value = "rows") int rows) {
		logger.info("====== Received request to generate testdata =======");
		for (int i = 1; i <= rows; i++) {
			final EmailMessage emailMsg = new EmailMessage();
			emailMsg.setTo("rs@test.com");
			emailMsg.setFrom("test@test.com");
			emailMsg.setBody("Test Messsage" + i);
			emailMsg.setSubject("Test Message" + i);
			emailMsg.setStatus(Status.NONE);
			em.persist(emailMsg);
		}
		return Response.ok(201).build();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/testdata")
	@Transactional
	public Response clearTestData() {
		logger.info("====== Received request to clear testdata =======");
		Query q = em.createQuery("delete * from EmailMessage");
		q.executeUpdate();
		return Response.ok(204).build();
	}

	@Async
	public void publishEmailMessage(EmailMessage msg) {
		logger.info("Publishing msg");
		redisTemplate.convertAndSend("mail", msg);
	}

	@Async
	@Transactional
	public void updateEmailMsgStatus(final Status status,
			final List<EmailMessage> msgList) {
		List<Integer> msgIds = getIds(msgList);
		if (msgIds.isEmpty()) {
			return;
		}
		Query q = em
				.createQuery("update EmailMessage set status = :status where id in (:ids)");
		q.setParameter("status", status);
		q.setParameter("ids",
				StringUtils.collectionToCommaDelimitedString(msgIds));
		int count = q.executeUpdate();
		logger.info("Total updated: " + count);
	}

	public List<Integer> getIds(final List<EmailMessage> msgs) {
		List<Integer> ids = new ArrayList<>();
		for (EmailMessage msg : msgs) {
			ids.add(msg.getId());
		}
		return ids;
	}
}
