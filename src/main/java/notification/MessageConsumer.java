package notification;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import notification.models.EmailMessage;

@Component
public class MessageConsumer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageConsumer.class);

	@Autowired(required = true)
	private JavaMailSender mailSender;

	public void receiveMessage(EmailMessage mailMsg) {
		LOGGER.info("!!!!!!Received <" + mailMsg + ">");
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setTo(mailMsg.getTo());
		smm.setText(mailMsg.getBody());
		smm.setFrom(mailMsg.getFrom());
		smm.setSubject(mailMsg.getSubject());
		LOGGER.info("Sending mail now.....");
		Future<Boolean> f = asyncSender(smm);
		try {
			boolean result = f.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Async
	public Future<Boolean> asyncSender(SimpleMailMessage smm) {
		mailSender.send(smm);
		return new AsyncResult<Boolean>(true);
	}
}
