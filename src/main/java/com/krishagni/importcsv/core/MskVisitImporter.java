package com.krishagni.importcsv.core;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
			importRecords(detail);	
			return ResponseEvent.response(detail.getObject());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@PlusTransactional
	private ResponseEvent<Object> importRecords(ImportObjectDetail<MskVisitDetail> detail) throws ParseException {
		importParticipants(detail.getObject());
		importVisit(detail.getObject());
		
		return null;
	}

	private ResponseEvent<CollectionProtocolRegistrationDetail> importParticipants(MskVisitDetail object) throws ParseException {
		CollectionProtocolRegistrationDetail cprDetail = new CollectionProtocolRegistrationDetail();
		
		cprDetail.setCpShortTitle(object.getCpShortTitle());
		cprDetail.setParticipant(new ParticipantDetail());
		cprDetail.setRegistrationDate(object.getVisitDate());
		
		// Adding participant Details
		cprDetail.setPpid(object.getPpid());
		cprDetail.getParticipant().setFirstName(object.getFirstName());
		cprDetail.getParticipant().setLastName(object.getLastName());
	
		// Setting PMI
		cprDetail.getParticipant().setPhiAccess(true);
		PmiDetail pmi = new PmiDetail();
		pmi.setMrn(object.getMrn());
		pmi.setSiteName(object.getSiteName());
		cprDetail.getParticipant().setPmi(pmi);

		if (checkParticipantExists(object)) {
			ResponseEvent<CollectionProtocolRegistrationDetail> participantResponse = cprSvc.updateRegistration(request(cprDetail));
			return participantResponse;
		} 
		return(cprSvc.createRegistration(request(cprDetail)));
	}

	private ResponseEvent<VisitDetail> importVisit(MskVisitDetail object) throws ParseException {
		VisitDetail visitDetail = new VisitDetail();
		
		// Setting Visit
		visitDetail.setCpShortTitle(object.getCpShortTitle());
		visitDetail.setPpid(object.getPpid());
		visitDetail.setEventLabel(object.getVisit() + object.getDay());
		visitDetail.setComments(object.getVisitComments());
		visitDetail.setVisitDate(object.getVisitDate());
		
		ResponseEvent<VisitDetail> visitResponse = visitService.addVisit(request(visitDetail));
		return(visitResponse);
	}
	
	private boolean checkParticipantExists(MskVisitDetail object) {
		if (daoFactory.getCprDao().getCprByCpShortTitleAndPpid(object.getCpShortTitle(), object.getPpid())!= null) {
			return true;
		}
		
		return false;
	}

	private <T> RequestEvent<T> request(T payload) {
		return new RequestEvent<T>(payload);
	}
}
