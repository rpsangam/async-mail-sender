package notification.service.repository;

import java.util.List;

import notification.models.EmailMessage;
import notification.models.Status;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface EmailMessageRepository extends Repository<EmailMessage, Long> {
	@Query("from EmailMessage em where em.status = ?1 and status = 'QUEUED'")
	List<EmailMessage> findByStatus(Status status);
}
