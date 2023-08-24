package ISTP.controller;

import ISTP.domain.bloodDonation.BloodTypeCategories;
import ISTP.dtos.member.MemberChangeDto;
import ISTP.dtos.member.MemberSaveForm;
import ISTP.domain.bloodDonation.accept.Accept;
import ISTP.dtos.member.MemberMyPageDto;
import ISTP.domain.bloodDonation.request.Request;
import ISTP.domain.member.Member;
import ISTP.dtos.request.MyAcceptDto;
import ISTP.dtos.request.MyRequestDto;
import ISTP.service.AcceptService;
import ISTP.service.MemberService;
import ISTP.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final RequestService requestService;
    private final AcceptService acceptService;

    //회원가입 로직
    @PostMapping("/save")
    public Long save(@Validated @RequestBody MemberSaveForm form, BindingResult bindingResult) {

        if (!(memberService.passwordReEnter(form.getPassword(), form.getRePassword()))) {
            bindingResult.reject("passwordMismatch", null);
        }

        if(bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            //이러고 어떻게 할지 고민 중
            throw new IllegalArgumentException("회원가입 오류발생");

        }

        BloodTypeCategories byBloodType = memberService.findByBloodType(form.getBloodType());
        Member member = new Member(form.getLoginId(), form.getPassword(),
                form.getName(), form.getNickname(), form.getAge(), form.isGender(),
                form.getPhoneNumber(), byBloodType, form.getEmail(), form.getAddress(), true);

        Long memberId = memberService.save(member);
        return memberId;
    }

    //회원가입 시 로그인 아이디 중복 확인 로직
    @PostMapping("/save/duplicate/loginId")
    @ResponseBody
    public String checkDuplicateLoginId(@RequestParam String loginId) {
         if(memberService.duplicatedLoginId(loginId)) {
             return "ok";
         }
         else {
             return "no";
         }
    }

    //회원가입 시 닉네임 중복 확인 로직
    @PostMapping("/save/duplicate/nickname")
    @ResponseBody
    public String checkDuplicateNickname(@RequestParam String nickname) {
        if(memberService.duplicatedNickname(nickname)) {
            return "ok";
        }
        else {
            return "no";
        }
    }

    //회원가입 시 닉네임 중복 확인 로직
    @PostMapping("/save/check_duplicate/phoneNumber")
    @ResponseBody
    public String checkDuplicatePhoneNumber(@RequestParam String phoneNumber) {
        if(memberService.duplicatedPhoneNumber(phoneNumber)) {
            return "ok";
        }
        else {
            return "no";
        }
    }

    //마이페이지에 뿌려줄 DTO
    @ResponseBody
    @GetMapping("/myPages/{memberId}")
    public MemberMyPageDto myPage(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);
        MemberMyPageDto myPageDto = new MemberMyPageDto(member);
        return myPageDto;
    }

    @GetMapping("/myPages/count/{memberId}")
    public Long count(@PathVariable Long memberId) {
        Member findMember = memberService.findById(memberId);
        memberService.findById(findMember.getId());
        return acceptService.count(findMember);
    }


    @PutMapping("/change/{memberId}")
    public ResponseEntity<?> change(@PathVariable Long memberId, @RequestBody MemberChangeDto memberChangeDto) {
        Member member = memberService.findById(memberId);
        ResponseEntity<?> responseEntity = memberService.changeMember(member, memberChangeDto.getPhoneNumber(), memberChangeDto.getUserNickname(),
                memberChangeDto.getAddress());
        return responseEntity;
    }

    //닉네임 수정
    /**
     * 수정시 닉네임과 관련된 모든 부분 고쳐야하는데 아직 안했음!! 
     */
    /*@PutMapping("/myPages/{memberId}/edit/nickname")
    public void editNickName(@PathVariable Long memberId, @RequestParam String nickname) {
        Member member = memberService.findById(memberId);
        memberService.changeNickname(member, nickname);
    }

    //주소 수정
    @PutMapping("/myPages/{memberId}/edit/address")
    public void editAddress(@PathVariable Long memberId, @RequestParam String address) {
        Member member = memberService.findById(memberId);
        memberService.changeAddress(member, address);
    }*/


    //내가 등록한 긴급헌혈 요청서 목록
    @ResponseBody
    @GetMapping("/myPages/{memberId}/myRequests")
    public List<MyRequestDto> myRequestList(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);
        List<Request> allByMemberNickname = requestService.findAllByMemberNickname(member.getNickname());
        List<MyRequestDto> requestDtos = new ArrayList<>();
        for (Request request : allByMemberNickname) {
            MyRequestDto requestDto = new MyRequestDto(request);
            requestDtos.add(requestDto);
        }
        return requestDtos;
    }

    //내가 요청 수락을 한 긴급헌혈 요청서 목록
    @ResponseBody
    @GetMapping("/myPages/{memberId}/myAccepts")
    public List<MyAcceptDto> myAcceptRequestList(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);
        List<Accept> accepts = acceptService.findByMember(member);
        List<MyAcceptDto> acceptDtos = new ArrayList<>();
        for (Accept accept : accepts) {
            Request request = accept.getRequest();
            MyAcceptDto acceptDto = new MyAcceptDto(request);
            acceptDtos.add(acceptDto);
        }
        return acceptDtos;
    }
}
