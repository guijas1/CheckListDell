package com.guijas1.checklistDell;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erroUpload", "Erro ao enviar arquivo: " + e.getMessage());
        System.err.println("⚠️ MultipartException capturada: " + e.getMessage());
        e.printStackTrace();
        return "redirect:/checklist";
    }


}
