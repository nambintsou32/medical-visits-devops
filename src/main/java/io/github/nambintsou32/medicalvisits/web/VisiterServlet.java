package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import io.github.nambintsou32.medicalvisits.domain.Patient;
import io.github.nambintsou32.medicalvisits.domain.Visiter;
import io.github.nambintsou32.medicalvisits.domain.VisiterId;
import io.github.nambintsou32.medicalvisits.persistence.MedecinRepository;
import io.github.nambintsou32.medicalvisits.persistence.PatientRepository;
import io.github.nambintsou32.medicalvisits.persistence.VisiterRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/visites/*")
public final class VisiterServlet extends HttpServlet {

    private MedecinRepository medecinRepository;
    private PatientRepository patientRepository;
    private VisiterRepository visiterRepository;

    @Override
    public void init() {
        EntityManagerFactory entityManagerFactory =
                DatabaseStartupListener.getEntityManagerFactory(
                    getServletContext()
                );

        medecinRepository = new MedecinRepository(entityManagerFactory);
        patientRepository = new PatientRepository(entityManagerFactory);
        visiterRepository = new VisiterRepository(entityManagerFactory);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getPathInfo();

        if ("/new".equals(path)) {
            showForm(request, response, null, null);
            return;
        }

        if ("/edit".equals(path)) {
            VisiterId id = visitIdFromRequest(
                request,
                "codeMed",
                "codePat",
                "visitDate"
            );

            Visiter visiter = visiterRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Visite introuvable"
                    ));

            showForm(request, response, visiter, null);
            return;
        }

        showList(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = requiredParameter(request, "action");

        try {
            switch (action) {
                case "create" -> createVisite(request);
                case "replace" -> replaceVisite(request);
                case "delete" -> deleteVisite(request);
                default -> throw new IllegalArgumentException(
                    "Action inconnue : " + action
                );
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/visites?message="
                    + action
            );
        } catch (IllegalArgumentException exception) {
            if ("delete".equals(action)) {
                request.setAttribute("error", exception.getMessage());
                showList(request, response);
            } else {
                showForm(request, response, null, exception.getMessage());
            }
        }
    }

    private void showList(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        List<Visiter> visites = visiterRepository.findAll();

        request.setAttribute("visites", visites);
        request.getRequestDispatcher("/WEB-INF/jsp/visites.jsp")
                .forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Visiter visiter,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("visiter", visiter);
        request.setAttribute("medecins", medecinRepository.findAll());
        request.setAttribute("patients", patientRepository.findAll());
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/visite-form.jsp")
                .forward(request, response);
    }

    private void createVisite(HttpServletRequest request) {
        visiterRepository.create(
            requiredParameter(request, "codeMed"),
            requiredParameter(request, "codePat"),
            requiredDate(request, "visitDate")
        );
    }

    private void replaceVisite(HttpServletRequest request) {
        VisiterId currentId = visitIdFromRequest(
            request,
            "currentCodeMed",
            "currentCodePat",
            "currentVisitDate"
        );

        visiterRepository.replace(
            currentId,
            requiredParameter(request, "codeMed"),
            requiredParameter(request, "codePat"),
            requiredDate(request, "visitDate")
        );
    }

    private void deleteVisite(HttpServletRequest request) {
        VisiterId id = visitIdFromRequest(
            request,
            "codeMed",
            "codePat",
            "visitDate"
        );

        if (!visiterRepository.deleteById(id)) {
            throw new IllegalArgumentException("Visite introuvable");
        }
    }

    private static VisiterId visitIdFromRequest(
            HttpServletRequest request,
            String codeMedParameter,
            String codePatParameter,
            String visitDateParameter
    ) {
        return new VisiterId(
            requiredParameter(request, codeMedParameter),
            requiredParameter(request, codePatParameter),
            requiredDate(request, visitDateParameter)
        );
    }

    private static LocalDate requiredDate(
            HttpServletRequest request,
            String name
    ) {
        try {
            return LocalDate.parse(requiredParameter(request, name));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                "La date " + name + " est invalide"
            );
        }
    }

    private static String requiredParameter(
            HttpServletRequest request,
            String name
    ) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Le champ " + name + " est obligatoire"
            );
        }

        return value.trim();
    }
}
