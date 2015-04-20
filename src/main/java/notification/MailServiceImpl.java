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


	// TODO: Add pagination
	@RequestMapping(method = RequestMethod.GET, value="/testdata")
	public List<EmailMessage> getEmailTestData(
			@RequestParam(value = "batchSize", required = false) int batchSize) {
		Query q = em.createQuery(
				" from EmailMessage em where em.status = 'NONE'",
				EmailMessage.class);
		List<EmailMessage> msgList = q.getResultList();
		return msgList;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "trigger")
	@Transactional
	public List<EmailMessage> triggerEmailJob(
			@RequestParam(value = "batchSize", required = false, defaultValue = "300") int batchSize) {
		Query q = em.createQuery(
				" from EmailMessage em where em.status = 'NONE'",
				EmailMessage.class);

		List<EmailMessage> msgList = q.getResultList();

		// Ideally lock the operation, so other load balancing node won't pick
		// up same set of records.
		logger.info("====== Triggering emails=======");
		updateEmailMsgStatus(Status.QUEUED, msgList);
		publishEmailMessage(msgList);
		return new ArrayList<EmailMessage>();
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
	private void publishEmailMessage(final EmailMessage msg) {
		logger.info("Publishing msg: " + msg.getId());
		redisTemplate.convertAndSend("mail", msg);
	}

	@Async
	private void publishEmailMessage(final List<EmailMessage> msgList) {
		for(EmailMessage msg: msgList) {
			publishEmailMessage(msg);
		}
	}

	@Async
	public void updateEmailMsgStatus(final Status status,
			final List<EmailMessage> msgList) {
		if (msgList.isEmpty()) {
			return;
		}

		Query q = em
				.createQuery("update EmailMessage set status = :status where id between :start and :end");
		q.setParameter("status", status);
		q.setParameter("start", msgList.get(0).getId());
		q.setParameter("end", msgList.get(msgList.size()-1).getId());
		int count = q.executeUpdate();
		logger.info("Total updated: " + count);
	}
}
