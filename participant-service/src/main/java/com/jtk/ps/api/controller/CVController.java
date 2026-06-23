package com.jtk.ps.api.controller;

import com.jtk.ps.api.dto.cv.CVGetResponse;
import com.jtk.ps.api.dto.cv.CVUpdateRequest;
import com.jtk.ps.api.service.IParticipantService;
import com.jtk.ps.api.util.Constant;
import com.jtk.ps.api.util.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cv")
public class CVController {

    @Autowired
    private IParticipantService participantService;

    @GetMapping("/detail/{id_cv}")
    public ResponseEntity<Object> getCVDetail(
            @PathVariable("id_cv") Integer idCv, 
            HttpServletRequest request) {
        try {
            // Ambil id dan role dari token JWT
            Integer idFromToken = (Integer) request.getAttribute(Constant.VerifyConstant.ID);
            Integer idRole = (Integer) request.getAttribute(Constant.VerifyConstant.ID_ROLE);
            String cookie = request.getHeader(Constant.PayloadResponseConstant.COOKIE);

            // Role 1 = PARTICIPANT → wajib validasi kepemilikan
            if (idRole != null && idRole == 1) {
                // Cek apakah cv ini milik participant yang login
                boolean isOwner = participantService.isCVOwnedByParticipant(idCv, idFromToken);
                if (!isOwner) {
                    return ResponseHandler.generateResponse(
                        "Access denied: CV does not belong to you", 
                        HttpStatus.FORBIDDEN
                    );
                }
            }

            CVGetResponse cv = participantService.getCVDetail(idCv, cookie);
            if (cv == null) {
                return ResponseHandler.generateResponse(
                    "CV not found", 
                    HttpStatus.NOT_FOUND
                );
            }
            return ResponseHandler.generateResponse("Get CV succeed", HttpStatus.OK, cv);

        } catch (HttpClientErrorException ex) {
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id_cv}")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<Object> updateCV(
            @PathVariable("id_cv") Integer idCv, 
            @Valid @RequestBody CVUpdateRequest cvUpdateRequest, 
            BindingResult bindingResult,
            HttpServletRequest request) {
        try {
            // Handle validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage())
                );
                return ResponseHandler.generateResponse(
                    "Validation failed: " + errors.values().stream()
                        .collect(Collectors.joining(", ")), 
                    HttpStatus.BAD_REQUEST, 
                    errors
                );
            }

            Integer idParticipant = (Integer) Objects.requireNonNull(
                request.getAttribute(Constant.VerifyConstant.ID),
                "User ID not found in request"
            );

            Boolean isSuccess = participantService.updateCV(idCv, cvUpdateRequest, idParticipant);
            
            if (Boolean.TRUE.equals(isSuccess)) {
                return ResponseHandler.generateResponse("Update cv succeed", HttpStatus.OK);
            } else {
                return ResponseHandler.generateResponse("Update cv failed", HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException ex) {
            return ResponseHandler.generateResponse("Invalid input: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException ex) {
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/mark-as-done")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<Object> markAsDone(HttpServletRequest request) {
        try {
            Boolean isSuccess = participantService.markAsDoneCv((Integer) Objects.requireNonNull(request.getAttribute(Constant.VerifyConstant.ID)));
            if (Boolean.TRUE.equals(isSuccess)) {
                return ResponseHandler.generateResponse("Mark as done succeed", HttpStatus.OK);
            }else{
                return ResponseHandler.generateResponse("Mark as done failed", HttpStatus.BAD_REQUEST);
            }
        } catch (HttpClientErrorException ex){
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @DeleteMapping(value = "/delete-competence/{id}")
    public ResponseEntity<Object> deleteCVCompetence(@PathVariable("id") Integer id) {
        try {
            participantService.deleteCVCompetence(id);
            return ResponseHandler.generateResponse("Delete CV competence succeed", HttpStatus.OK);
        } catch (HttpClientErrorException ex){
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @DeleteMapping(value = "/delete-jobscope/{id}")
    public ResponseEntity<Object> deleteCVJobscope(@PathVariable("id") Integer id) {
        try {
            participantService.deleteCVJobscope(id);
            return ResponseHandler.generateResponse("Delete CV jobscope succeed", HttpStatus.OK);
        } catch (HttpClientErrorException ex){
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/recap")
    @PreAuthorize("hasAnyAuthority('COMMITTEE', 'HEAD_STUDY_PROGRAM')")
    public ResponseEntity<Object> recapCompetence(HttpServletRequest request) {
        try {
            Integer idProdi = (Integer) Objects.requireNonNull(request.getAttribute(Constant.VerifyConstant.ID_PRODI));
            String cookie = request.getHeader(Constant.PayloadResponseConstant.COOKIE);
            return ResponseHandler.generateResponse("Get recap competence succeed", HttpStatus.OK, participantService.recapCompetence(cookie, idProdi));
        } catch (HttpClientErrorException ex){
            return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{id_cv}/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public void exportCV(HttpServletRequest request, HttpServletResponse response, @PathVariable("id_cv") Integer idCv) throws IOException {
        String cookie = request.getHeader(Constant.PayloadResponseConstant.COOKIE);
        response.setHeader("Content-Disposition", "attachment; filename=\"CV_Mahasiswa_" + idCv + ".pdf\"");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        participantService.exportCV(cookie, idCv, response);
    }
}
