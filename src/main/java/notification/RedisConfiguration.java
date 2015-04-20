package notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import notification.models.EmailMessage;

@Configuration
@PropertySource("classpath:notification.properties")
public class RedisConfiguration {

	JacksonJsonRedisSerializer<EmailMessage> serializer = new JacksonJsonRedisSerializer<EmailMessage>(
			EmailMessage.class);

	@Bean
	public RedisMessageListenerContainer container(
			RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("mail"));
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(MessageConsumer receiver) {
		MessageListenerAdapter a = new MessageListenerAdapter(
				receiver, "receiveMessage");
		a.setSerializer(serializer);

		return a;

	}

	@Bean
	public MessageConsumer receiver() {
		return new MessageConsumer();
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, EmailMessage> redisTemplate(
			RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, EmailMessage> redisTemplate = new RedisTemplate<String, EmailMessage>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(serializer);
		return redisTemplate;
	}

}
