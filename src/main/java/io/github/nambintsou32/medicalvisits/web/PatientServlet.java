package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Patient;
import io.github.nambintsou32.medicalvisits.domain.Sexe;
import io.github.nambintsou32.medicalvisits.persistence.PatientRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/patients/*")
public final class PatientServlet extends HttpServlet {

    private PatientRepository patientRepository;

    @Override
    public void init() {
        EntityManagerFactory entityManagerFactory =
                DatabaseStartupListener.getEntityManagerFactory(
                    getServletContext()
                );

        patientRepository = new PatientRepository(entityManagerFactory);
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
            String codePat = requiredParameter(request, "codePat");

            Patient patient = patientRepository.findByCode(codePat)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Patient introuvable : " + codePat
                    ));

            showForm(request, response, patient, null);
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
                case "create" -> createPatient(request);
                case "update" -> updatePatient(request);
                case "delete" -> deletePatient(request);
                default -> throw new IllegalArgumentException(
                    "Action inconnue : " + action
                );
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/patients?message="
                    + action
            );
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());

            if ("delete".equals(action)) {
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
        String search = trimToNull(request.getParameter("search"));
        String searchBy = trimToNull(request.getParameter("searchBy"));

        List<Patient> patients;

        if (search == null) {
            patients = patientRepository.findAll();
        } else if ("code".equals(searchBy)) {
            patients = patientRepository.findByCode(search)
                    .map(List::of)
                    .orElseGet(List::of);
        } else {
            patients = patientRepository.findByNomContaining(search);
            searchBy = "nom";
        }

        request.setAttribute("patients", patients);
        request.setAttribute("search", search);
        request.setAttribute("searchBy", searchBy);
        request.getRequestDispatcher("/WEB-INF/jsp/patients.jsp")
                .forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Patient patient,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("patient", patient);
        request.setAttribute("sexes", Sexe.values());
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/patient-form.jsp")
                .forward(request, response);
    }

    private void createPatient(HttpServletRequest request) {
        patientRepository.create(new Patient(
            requiredParameter(request, "codePat"),
            requiredParameter(request, "nom"),
            requiredParameter(request, "prenom"),
            Sexe.valueOf(requiredParameter(request, "sexe")),
            requiredParameter(request, "adresse")
        ));
    }

    private void updatePatient(HttpServletRequest request) {
        String codePat = requiredParameter(request, "codePat");

        Patient patient = patientRepository.findByCode(codePat)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Patient introuvable : " + codePat
                ));

        patient.setNom(requiredParameter(request, "nom"));
        patient.setPrenom(requiredParameter(request, "prenom"));
        patient.setSexe(
            Sexe.valueOf(requiredParameter(request, "sexe"))
        );
        patient.setAdresse(requiredParameter(request, "adresse"));

        patientRepository.update(patient);
    }

    private void deletePatient(HttpServletRequest request) {
        String codePat = requiredParameter(request, "codePat");

        if (!patientRepository.deleteByCode(codePat)) {
            throw new IllegalArgumentException(
                "Patient introuvable : " + codePat
            );
        }
    }

    private static String requiredParameter(
            HttpServletRequest request,
            String name
    ) {
        String value = trimToNull(request.getParameter(name));

        if (value == null) {
            throw new IllegalArgumentException(
                "Le champ " + name + " est obligatoire"
            );
        }

        return value;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
