package com.myweb.www.repository;

import java.util.List;

import com.myweb.www.domain.ReqFileVO;
import com.myweb.www.domain.RequestDTO;
import com.myweb.www.domain.RequestQuestionVO;
import com.myweb.www.domain.RequestVO;


public interface RequestDAO {

	List<RequestQuestionVO> list();

	List<RequestQuestionVO> list2();

	List<RequestQuestionVO> list3();

	List<RequestQuestionVO> list3_2();

void insert(RequestVO rvo);

/* void insert_store(requestAnswer rvo); */

	/*
	 * List<RequestQuestionVO> list2_1();
	 * 
	 * void insert_store_1(requestAnswer rvo);
	 * 
	 * void insert_store_2_1(requestAnswer rvo);
	 * 
	 * void insert_store_2_2(requestAnswer rvo);
	 */
	/*
	 * long selectNm(String userid);
	 */
	List<RequestVO> selectrequset(String id);

	long selectNm();

	List<RequestQuestionVO> list2_1();

	int request_alarm(String userId);

	String selectOneBno(String requestId);

	long selectOneBno(long requestNm);

	List<ReqFileVO> selectOneRVO(String requestId);

	List<RequestVO> getRequest_list(long requestNm);

	void quest_alarm_submit(long reqNm_q);



void checked(long requestNm);

	Boolean req_check(long requestNm);

	List<RequestVO> selectrequset_read(String id);

	int req_cancel(long reqNm);

	List<RequestVO> getList_user(String id);

	int request_alarm_user(String userId);





}