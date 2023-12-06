package com.myweb.www.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.myweb.www.domain.FileVO;
import com.myweb.www.handler.ProfileFileHandler;
import com.myweb.www.security.AuthMember;
import com.myweb.www.security.AuthVO;
import com.myweb.www.security.MemberVO;
import com.myweb.www.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/member/**")
@Controller
public class MemberController {

   @Inject
   private BCryptPasswordEncoder bcEncoder;
   @Inject
   private MemberService msv;
   @Inject
   private ProfileFileHandler pfh;

   @GetMapping("/register")
   public void register() {
   }

   // 회원 등록
   @PostMapping("/register")
   public String register(MemberVO mvo, @RequestParam(name = "profile", required = false) MultipartFile[] files,
         Model m) {
      int isOk = 1;
      if (mvo.getPw() != null) {
         mvo.setPw(bcEncoder.encode(mvo.getPw())); // 암호화해서 넣음
      }
      isOk *= msv.register(mvo);
      String id = mvo.getId();
      FileVO fvo = null;
      if (files[0] != null) {
         fvo = pfh.uploadFiles(files[0], id);
      }
      isOk *= msv.insert(id, fvo);
      log.info(isOk > 0 ? "성공" : "실패");
      return "/member/login";
   }

   @GetMapping("/companyRegister")
   public void companyRegister() {
   }
   
   // 업체 등록
   @PostMapping("/companyRegister")
   public String companyRegister(MemberVO mvo, @RequestParam(name = "profile", required = false) MultipartFile[] files,
         Model m) {
      int isOk = 1;
      if (mvo.getPw() != null) {
         mvo.setPw(bcEncoder.encode(mvo.getPw())); // 암호화해서 넣음
      }
      isOk *= msv.companyRegister(mvo);
      String id = mvo.getId();
      FileVO fvo = null;
      if (files[0] != null) {
         fvo = pfh.uploadFiles(files[0], id);
      }
      isOk *= msv.insert(id, fvo);
      log.info(isOk > 0 ? "성공" : "실패");
      return "/member/login";
   }

   @GetMapping("/login")
   public void login() {
   }

   // 로그인 폼을 거치지 않고 바로 로그인
   @PostMapping("/loginWithoutForm")
   public String loginWithoutForm(@RequestParam String id) {

      List<GrantedAuthority> roles = new ArrayList<>(1);
      String roleStr = "ROLE_USER";
      roles.add(new SimpleGrantedAuthority(roleStr));
      
      MemberVO mvo = msv.memberDetail(id);
      AuthVO avo = msv.getAuthList(id);
      List<AuthVO> authList = new ArrayList<AuthVO>();
      authList.add(avo);
      mvo.setAuthVOList(authList);
      mvo.setPw("");   
      AuthMember authMember = new AuthMember(mvo);   
      
      Authentication auth = new UsernamePasswordAuthenticationToken(authMember, null, roles);
      SecurityContextHolder.getContext().setAuthentication(auth);
      return "index";
   }

   // 카카오 인증
   @RequestMapping(value = "/kakao", method = RequestMethod.GET)
   public String kakao(@RequestParam(required = false) String code, @RequestParam String ok, Model m) {
      String kakaoUrl = "https://kauth.kakao.com/oauth/authorize?client_id=e7f7342b45a67c5286814656c21b3bdd&redirect_uri=http://localhost:8088/member/";
      kakaoUrl += ok;
      kakaoUrl += "&response_type=code";
      m.addAttribute("kakaoUrl", kakaoUrl);
      return "/member/kakaoLogin";
   }

   // 카카오 로그인
   @RequestMapping(value = "kakaologin")
   public String kakaologin(@RequestParam(required = false) String code, HttpSession ses, Model m,
         RedirectAttributes re) {
      String ok = "kakaologin";

      JsonNode token = msv.getAccessToken(code, ok);
      JsonNode user = msv.getUserInfo(token, "kakao");
      log.info("user:" + user);

      String url = msv.kakaoLogin(user, ses, m);

      return url;
   }

   // 카카오 회원가입
   @RequestMapping(value = "kakaojoin")
   public String kakaojoin(@RequestParam(required = false) String code, HttpSession ses, Model m) {
      String ok = "kakaojoin";
      JsonNode token = msv.getAccessToken(code, ok);
      JsonNode user = msv.getUserInfo(token, "kakao");

      String url = msv.kakaojoin(user, m);

      return url;
   }

   // 네이버
   @RequestMapping(value = "naver")
   public String naver(@RequestParam(required = false) String code, @RequestParam String ok, Model m) {
      String naverUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=AGtr4pd5S4hkBnMZnEyo&redirect_uri=http://localhost:8088/member/";
      naverUrl += ok;
      naverUrl += "&state=" + "test";

      log.info(naverUrl);

      m.addAttribute("naverUrl", naverUrl);
      return "/member/naverLogin";
   }

   // 네이버 로그인
   @RequestMapping(value = "naverLogin")
   public String naverlogin(@RequestParam(required = false) String code, @RequestParam("state") String state,
         HttpSession ses, Model m) {
      JsonNode token = msv.getAccessToken(code, state, "naver");
      JsonNode user = msv.getUserInfo(token, "naver");
      log.info("user:" + user);

      String url = msv.naverLogin(user, ses, m);

      return url;
   }

   // 네이버 회원가입
   @RequestMapping(value = "naverjoin")
   public String naverjoin(@RequestParam("code") String code, @RequestParam("state") String state, Model m) {
      JsonNode token = msv.getAccessToken(code, state, "naver");
      JsonNode user = msv.getUserInfo(token, "naver");
      String url = msv.naverjoin(user, m);
      return url;
   }

   // 로그인 실패 시
   @PostMapping("/login")
   public String loginPost(HttpServletRequest request, RedirectAttributes re) {
      re.addAttribute("id", request.getAttribute("id"));
      re.addAttribute("errMsg", request.getAttribute("errMsg"));
      return "redirect:/member/login";
   }

   @RequestMapping(value = "/logout")
   public String logout(HttpSession session) {
      msv.kakaoLogout((String) session.getAttribute("access_Token"));
      session.removeAttribute("access_Token");
      session.removeAttribute("userId");
      return "index";
   }

   // 회원 리스트(확인)
   /*
    * @GetMapping("/list") public String list(Model model, PagingVO pagingVO) { //
    * MemberDTO mdto = msv.getMemberList(pagingVO); List<MemberDTO> list =
    * msv.getMemberList(pagingVO); model.addAttribute("list", list); // 총 페이지 갯수
    * int totalCount = msv.getTotalCount(pagingVO); PagingHandler ph = new
    * PagingHandler(totalCount, pagingVO); model.addAttribute("ph", ph); return
    * "/member/list";
    * 
    * }
    */

   @GetMapping({ "/detail" })
   public void detail(Model model, @RequestParam("id") String id) {
      MemberVO mvo = msv.memberDetail(id);
      model.addAttribute("mvo", mvo);
   }

   @GetMapping("/modify")
   public void modify(@RequestParam("id") String id, Model m) {
      m.addAttribute("mvo", msv.memberDetail(id));
   }

   // 회원 수정(확인)
   @PostMapping({ "/modify" })
   public void modify(MemberVO mvo, Model m, HttpServletRequest req, HttpServletResponse res) {
      int isOk = 3;
      if (mvo.getPw() == null || mvo.getPw().isEmpty()) {
         isOk = msv.modifyPwdEmpty(mvo);
      } else {
         mvo.setPw(bcEncoder.encode(mvo.getPw()));
         isOk = msv.modify(mvo);
      }
      logout(req, res);

      m.addAttribute("isOk", isOk);
   }

   // 회원 탈퇴(확인)
   @GetMapping("/remove")
   public String removeMember(@RequestParam("id") String id, Model m, HttpServletRequest req,
         HttpServletResponse res) {
      int isOk = msv.remove(id);
      logout(req, res);
      m.addAttribute("isOkDel", isOk);
      return "index";

   }

   @GetMapping("/index")
   public String index() {
      return "index";
   }

   // 비밀번호 변경 전 본인 인증 페이지로 이동
   @GetMapping("/checkMemberInfo")
   public void checkMemberInfo() {

   }

   // 본인 인증 확인
   @PostMapping("/checkMemberInfo")
   public String postCheckMemberInfo(MemberVO mvo, RedirectAttributes re, Model m) {
      MemberVO detail = msv.memberDetail(mvo.getId());
      // 입력한 정보가 기존 정보와 전부 일치하는지 확인
      if (detail.getId().equals(mvo.getId()) && detail.getUserNm().equals(mvo.getUserNm())) {
         m.addAttribute("id", mvo.getId());
         return "/member/modifyPw";
      } else {
         m.addAttribute("msg", "해당 사원은 존재하지 않습니다.");
         m.addAttribute("url", "/member/checkMemberInfo");
         return "alert";
      }
   }

   @GetMapping("/modifyPw")
   public void modifyPw() {
   }

   // 비밀번호 변경
   @PostMapping("/modifyPw")
   public String PostModifyPw(@RequestParam("pw") String pw, @RequestParam("pw2") String pw2,
         @RequestParam("id") String id, Model m) {
      if (pw.equals(pw2)) {
         String password = bcEncoder.encode(pw);
         log.info("id: " + id + "/ pw: " + pw);
         int isOk = msv.updatePw(id, password);
         if (isOk > 0) {
            m.addAttribute("msg", "변경이 완료되었습니다.");
            m.addAttribute("url", "/member/login");
            return "alert";
         } else {
            m.addAttribute("msg", "비밀번호 변경 실패.");
            m.addAttribute("url", "/member/modifyPw");
            return "alert";
         }
      } else {
         m.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
         m.addAttribute("url", "/member/checkMemberInfo");
         return "alert";
      }
   }
   
   //마이페이지
   @GetMapping("/myPage")
   public void myPage(@RequestParam String id, Model m, HttpServletRequest request) {
      FileVO fvo = msv.getFile(id);
      m.addAttribute("fvo", fvo);
   }

   // 아이디 일치하는지 확인
   @PostMapping(value = "/checkId", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> checkId(@RequestBody MemberVO mvo, Model m) {
      int isOk = msv.checkId(mvo.getId());
      return isOk > 0 ? new ResponseEntity<String>("1", HttpStatus.OK)
            : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
   }

   private void logout(HttpServletRequest req, HttpServletResponse res) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      new SecurityContextLogoutHandler().logout(req, res, authentication);

   }

}