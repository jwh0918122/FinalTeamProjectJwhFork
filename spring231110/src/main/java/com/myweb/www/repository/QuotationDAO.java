package com.myweb.www.repository;

import java.util.List;

import com.myweb.www.domain.QuotationVO;
import com.myweb.www.domain.RequestVO;

public interface QuotationDAO {

	void submit(QuotationVO avo);

	void quatation_submit(QuotationVO qvo);

	List<RequestVO> getList_user(String id);

	void checked(long qutationNm);

	List<RequestVO> getRequest_list_user(long qutationNm);

	int request_alarm_user(String userId);

	List<QuotationVO> getList_read_user(String id);

	QuotationVO getQuotation(long quotationNm);

}