package com.krishagni.openspecimen.msk2.importer;

import java.text.ParseException;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRegistrationDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantDetail;
import com.krishagni.catissueplus.core.biospecimen.events.PmiDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolRegistrationService;
import com.krishagni.catissueplus.core.biospecimen.services.VisitService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.importer.events.ImportObjectDetail;
import com.krishagni.catissueplus.core.importer.services.ObjectImporter;

@Configurable
public class MskVisitImporter implements ObjectImporter<MskVisitDetail, MskVisitDetail> {
	
	@Autowired
	private DaoFactory daoFactory;
	
	@Autowired
	private CollectionProtocolRegistrationService cprSvc;
	
	@Autowired
	private VisitService visitService;
	
	@Override
	public ResponseEvent<MskVisitDetail> importObject(
			RequestEvent<ImportObjectDetail<MskVisitDetail>> req) {
		try {
			ImportObjectDetail<MskVisitDetail> detail = req.getPayload();
			importRecord(detail);	
			return ResponseEvent.response(detail.getObject());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@PlusTransactional
	private ResponseEvent<Object> importRecord(ImportObjectDetail<MskVisitDetail> detail) throws ParseException {
		importParticipant(detail.getObject());
		importVisit(detail.getObject());
		
		return null;
	}

	private ResponseEvent<CollectionProtocolRegistrationDetail> importParticipant(MskVisitDetail object) throws ParseException {
		CollectionProtocolRegistrationDetail cprDetail = new CollectionProtocolRegistrationDetail();
		
		cprDetail.setCpShortTitle(object.getCpShortTitle());
		cprDetail.setParticipant(toParticipant(object));
		
		cprDetail.setRegistrationDate(object.getVisitDate());
		cprDetail.setPpid(object.getPpid());
		
		ResponseEvent<CollectionProtocolRegistrationDetail> participantResponse = null;
		if (doesParticipantExists(object.getCpShortTitle(), object.getPpid())) {
			participantResponse = cprSvc.updateRegistration(request(cprDetail));
		} else {
			participantResponse = cprSvc.createRegistration(request(cprDetail));
		}
		
		return participantResponse;
	}
	
	private ParticipantDetail toParticipant(MskVisitDetail object) {
		
		ParticipantDetail participant = new ParticipantDetail();
		participant.setFirstName(object.getFirstName());
		participant.setLastName(object.getLastName());
		participant.setPhiAccess(true);

		PmiDetail pmi = new PmiDetail();
		pmi.setMrn(object.getMrn());
		pmi.setSiteName(object.getSiteName());
		participant.setPmi(pmi);
		
		return participant;
	}

	private ResponseEvent<VisitDetail> importVisit(MskVisitDetail object) throws ParseException {
		VisitDetail visitDetail = new VisitDetail();
		String eventLabel = object.getStudyPhase() + "#" + object.getEventPoint();

		// Setting Visit
		visitDetail.setCpShortTitle(object.getCpShortTitle());
		visitDetail.setPpid(object.getPpid());
		visitDetail.setEventLabel(eventLabel);
		visitDetail.setComments(object.getVisitComments());
		visitDetail.setVisitDate(object.getVisitDate());
		
		ResponseEvent<VisitDetail> visitResponse = null;
		
		if (doesVisitExists(object)) {
			visitDetail.setId(getVisitByEventLabelAndVisitDate(object).getId());
			visitDetail.setName(getVisitByEventLabelAndVisitDate(object).getName());
			visitResponse = visitService.updateVisit(request(visitDetail));
		} else {
			visitResponse = visitService.addVisit(request(visitDetail));
		}
		
		return visitResponse;
	}

	private boolean doesParticipantExists(String cpShortTitle, String ppid) {
		return daoFactory.getCprDao().getCprByCpShortTitleAndPpid(cpShortTitle, ppid)!= null ? true : false;
	}
	
	private boolean doesVisitExists(MskVisitDetail object) {
		return getVisitByEventLabelAndVisitDate(object) != null ? true : false ;
	}
	
	private Visit getVisitByEventLabelAndVisitDate(MskVisitDetail object) {
		CollectionProtocolRegistration cpr = daoFactory
				.getCprDao()
				.getCprByCpShortTitleAndPpid(object.getCpShortTitle(), object.getPpid());

		Visit matchedVisit = cpr.getVisits().stream()
		.filter(visit -> isVisitOfSameEvent(visit, object))
		.filter(visit -> DateUtils.isSameDay(visit.getVisitDate(), object.getVisitDate()))
		.findAny().orElse(null);
		
		return matchedVisit;
	}
	
	private boolean isVisitOfSameEvent(Visit visit, MskVisitDetail object) {
		String eventLabel = object.getStudyPhase() + "#" + object.getEventPoint();
		
		return visit.isOfEvent(eventLabel);
	}
				
	private <T> RequestEvent<T> request(T payload) {
		return new RequestEvent<T>(payload);
	}
}