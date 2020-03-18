package com.vmware.scst.samples;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;

@Slf4j
@SpringBootTest
@AutoConfigureStubRunner(
        stubsMode = REMOTE,
        repositoryRoot = "git://git@github.com:dmfrey/scst-contracts.git",
        ids = { "com.vmware.scst.samples:messages:+:" },
        consumerName = "messaging",
        generateStubs = true,
        mappingsOutputFolder = "build/mappings"
)
@Import( ScstStubrunnerTests.TestApplication.class )
public class ScstStubrunnerTests {

    @Autowired
    StubTrigger stubTrigger;

    @MockBean
    Gateway mockGateway;

    @Test
    public void test() {

        UUID fakeId = UUID.fromString( "131e0977-60e0-47e0-b7c2-80684f9acd5d" );

        stubTrigger.trigger( "send updated" );

        verify( mockGateway ).save( fakeId );

    }

    @SpringBootApplication
    @Import( TestChannelBinderConfiguration.class )
    public static class TestApplication {

        @Bean
        // TODO: Change the name of this method to 'UpdateChannel'
        Consumer<UpdatedEventMessage> client( final Gateway gateway ) {

            return data -> {
                    log.info( "data: {}", data );

                    gateway.save( data.getEvent().getId() );

            };
        }

    }

}

@Value
class UpdatedEventMessage {

    Event event;
    String eventType;

    @JsonCreator
    UpdatedEventMessage(
            @JsonProperty( "event" ) final Event event,
            @JsonProperty( "eventType" ) final String eventType
    ) {

        this.event = event;
        this.eventType = eventType;

    }
}

@Value
@JsonIgnoreProperties( ignoreUnknown = true )
class Event {

    LocalDate date;
    UUID id;

    @JsonCreator
    public Event(
            @JsonProperty( "id" ) final UUID id,
            @JsonProperty( "date" ) final LocalDate date
    ) {

        this.id = id;
        this.date = date;

    }

}

interface Gateway {

    void save( UUID id );

}