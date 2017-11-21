package no.fint.consumer.organisasjonselement;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.CacheService;
import no.fint.cache.FintCache;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrganisasjonselementCacheService extends CacheService<FintResource<Organisasjonselement>> {

    public static final String MODEL = Organisasjonselement.class.getSimpleName().toLowerCase();

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    public OrganisasjonselementCacheService() {
        super(MODEL, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT);
    }

    @PostConstruct
    public void init() {
        Arrays.stream(props.getOrgs()).forEach(this::createCache);
    }

    @Scheduled(initialDelayString = ConsumerProps.CACHE_INITIALDELAY_ORGANISASJONSELEMENT, fixedRateString = ConsumerProps.CACHE_FIXEDRATE_ORGANISASJONSELEMENT)
    public void populateCacheAll() {
        Arrays.stream(props.getOrgs()).forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
        flush(orgId);
        populateCache(orgId);
    }

    private void populateCache(String orgId) {
        log.info("Populating organisasjonselement cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }

    public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementById(String orgId, String organisasjonsId) {
        return getOne(orgId, (fintResource) -> fintResource.getResource().getOrganisasjonsId().getIdentifikatorverdi().equals(organisasjonsId));
    }

    public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementByKode(String orgId, String kode) {
        return getOne(orgId, (fintResource) -> fintResource.getResource().getOrganisasjonsKode().getIdentifikatorverdi().equals(kode));
    }

	public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementByNummer(String orgNummer, String kode) {
        return getOne(orgNummer, (fintResource) -> fintResource.getResource().getOrganisasjonsnummer().getIdentifikatorverdi().equals(kode));
	}
	
	@Override
	public void onAction(Event event) {
		update(event, new TypeReference<List<FintResource<Organisasjonselement>>>() {
		});
	}
}
