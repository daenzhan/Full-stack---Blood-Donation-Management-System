package org.example.analysisservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RecommendationService {

    private static final double MIN_HEMOGLOBIN = 12.5;
    private static final double MAX_ALT_LEVEL = 40.0;
    private static final int DEFERRAL_DAYS_LOW_HEMOGLOBIN = 30;
    private static final int NORMAL_DONATION_INTERVAL_DAYS = 60;

    public DonorRecommendation generateRecommendations(Analysis analysis) {
        log.info("Generating recommendations for analysis ID: {}", analysis.getAnalysis_id());

        List<String> rejectionReasons = new ArrayList<>();
        List<String> healthRecommendations = new ArrayList<>();
        List<String> dietaryRecommendations = new ArrayList<>();

        // Проверка инфекционных заболеваний
        checkInfectiousDiseases(analysis, rejectionReasons);

        // Проверка биохимических показателей
        checkBiochemicalIndicators(analysis, rejectionReasons, healthRecommendations);

        // Проверка гемоглобина
        checkHemoglobin(analysis, rejectionReasons, dietaryRecommendations);

        // Генерация диетических рекомендаций по группе крови
        generateBloodTypeRecommendations(analysis, dietaryRecommendations);

        // Общие рекомендации
        addGeneralRecommendations(healthRecommendations, dietaryRecommendations);

        // Определение статуса и даты следующей донации
        EligibilityStatus status = calculateEligibilityStatus(rejectionReasons);
        LocalDateTime nextDonationDate = calculateNextDonationDate(analysis, status, rejectionReasons);

        // Формирование информации о группе крови
        String bloodGroupInfo = formatBloodGroupInfo(analysis);

        return DonorRecommendation.builder()
                .analysisId(analysis.getAnalysis_id())
                .donorId(analysis.getDonor_id())
                .donationId(analysis.getDonation_id())
                .eligibilityStatus(status)
                .nextDonationDate(nextDonationDate)
                .healthRecommendations(healthRecommendations)
                .dietaryRecommendations(dietaryRecommendations)
                .rejectionReasons(rejectionReasons)
                .bloodGroupInfo(bloodGroupInfo)
                .analysisDate(analysis.getAnalysis_date())
                .build();
    }

    private void checkInfectiousDiseases(Analysis analysis, List<String> rejectionReasons) {
        if (isPositive(analysis.getHiv())) {
            rejectionReasons.add("Обнаружена ВИЧ-инфекция - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getHepatitisB())) {
            rejectionReasons.add("Обнаружен гепатит B - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getHepatitisC())) {
            rejectionReasons.add("Обнаружен гепатит C - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getSyphilis())) {
            rejectionReasons.add("Обнаружен сифилис - отстранение до полного излечения и подтверждения отрицательных анализов");
        }

        if (isPositive(analysis.getBrucellosis())) {
            rejectionReasons.add("Обнаружена бруцеллезная инфекция - отстранение на 6 месяцев после выздоровления");
        }
    }

    private void checkBiochemicalIndicators(Analysis analysis, List<String> rejectionReasons,
                                            List<String> healthRecommendations) {
        if (analysis.getAlt_level() != null) {
            if (analysis.getAlt_level() > MAX_ALT_LEVEL) {
                String message = String.format("Повышенный уровень ALT (%.1f Ед/л при норме до %.1f Ед/л)",
                        analysis.getAlt_level(), MAX_ALT_LEVEL);
                healthRecommendations.add(message + ". Рекомендуется консультация гепатолога");

                if (analysis.getAlt_level() > MAX_ALT_LEVEL * 2) {
                    rejectionReasons.add("Значительно повышен уровень ALT - требуется обследование");
                }
            }
        }
    }

    private void checkHemoglobin(Analysis analysis, List<String> rejectionReasons,
                                 List<String> dietaryRecommendations) {
        if (analysis.getHemoglobin() != null) {
            if (analysis.getHemoglobin() < MIN_HEMOGLOBIN) {
                String message = String.format("Низкий уровень гемоглобина (%.1f г/дл при минимальной норме %.1f г/дл)",
                        analysis.getHemoglobin(), MIN_HEMOGLOBIN);
                rejectionReasons.add(message);

                dietaryRecommendations.add("Низкий уровень гемоглобина. Рекомендуется употреблять:");
                dietaryRecommendations.add("- Красное мясо, печень");
                dietaryRecommendations.add("- Шпинат, бобовые, гречку");
                dietaryRecommendations.add("- Гранаты, яблоки, свеклу");
                dietaryRecommendations.add("- Витамин C для улучшения усвоения железа");
            } else if (analysis.getHemoglobin() < MIN_HEMOGLOBIN + 1.0) {
                dietaryRecommendations.add("Уровень гемоглобина близок к нижней границе нормы. Обогатите рацион железосодержащими продуктами");
            }
        }
    }

    private void generateBloodTypeRecommendations(Analysis analysis, List<String> dietaryRecommendations) {
        if (analysis.getBlood_group() != null) {
            String bloodType = analysis.getBlood_group();
            String rhesus = analysis.getRhesus_factor();

            dietaryRecommendations.add("Рекомендации по питанию для группы крови " + bloodType + rhesus + ":");

            switch (bloodType) {
                case "O":
                    dietaryRecommendations.add("- Высокобелковая диета: мясо, рыба, птица");
                    dietaryRecommendations.add("- Овощи: брокколи, шпинат, морская капуста");
                    dietaryRecommendations.add("- Ограничить: молочные продукты, пшеницу");
                    break;
                case "A":
                    dietaryRecommendations.add("- Вегетарианская диета: овощи, тофу, соевые продукты");
                    dietaryRecommendations.add("- Морепродукты, злаки, фрукты");
                    dietaryRecommendations.add("- Ограничить: красное мясо, молочные продукты");
                    break;
                case "B":
                    dietaryRecommendations.add("- Сбалансированная диета: мясо, молочные продукты, злаки");
                    dietaryRecommendations.add("- Зеленые овощи, яйца, нежирные молочные продукты");
                    dietaryRecommendations.add("- Ограничить: курицу, гречку, кукурузу");
                    break;
                case "AB":
                    dietaryRecommendations.add("- Смешанная диета: морепродукты, тофу, молочные продукты");
                    dietaryRecommendations.add("- Зеленые овощи, ананасы, клюква");
                    dietaryRecommendations.add("- Ограничить: красное мясо, кукурузу, гречку");
                    break;
            }
        }
    }

    private void addGeneralRecommendations(List<String> healthRecommendations,
                                           List<String> dietaryRecommendations) {
        // Общие рекомендации по здоровью
        healthRecommendations.add("Пейте достаточное количество воды - не менее 2 литров в день");
        healthRecommendations.add("Избегайте употребления алкоголя за 48 часов до донации");
        healthRecommendations.add("Высыпайтесь (7-8 часов в сутки)");
        healthRecommendations.add("Воздержитесь от курения за 2 часа до и после донации");

        // Общие диетические рекомендации
        dietaryRecommendations.add("Общие рекомендации для доноров:");
        dietaryRecommendations.add("- Сбалансированное питание с достаточным количеством белка");
        dietaryRecommendations.add("- Регулярное употребление железосодержащих продуктов");
        dietaryRecommendations.add("- Ограничение жирной пищи перед донацией");
    }

    private EligibilityStatus calculateEligibilityStatus(List<String> rejectionReasons) {
        if (rejectionReasons.stream().anyMatch(reason -> reason.contains("постоянное отстранение"))) {
            return EligibilityStatus.PERMANENTLY_DEFERRED;
        } else if (!rejectionReasons.isEmpty()) {
            return EligibilityStatus.TEMPORARILY_DEFERRED;
        } else {
            return EligibilityStatus.ELIGIBLE;
        }
    }

    private LocalDateTime calculateNextDonationDate(Analysis analysis, EligibilityStatus status,
                                                    List<String> rejectionReasons) {
        LocalDateTime now = LocalDateTime.now();

        switch (status) {
            case ELIGIBLE:
                return now.plusDays(NORMAL_DONATION_INTERVAL_DAYS);

            case TEMPORARILY_DEFERRED:
                if (rejectionReasons.stream().anyMatch(reason -> reason.contains("гемоглобин"))) {
                    return now.plusDays(DEFERRAL_DAYS_LOW_HEMOGLOBIN);
                } else if (rejectionReasons.stream().anyMatch(reason -> reason.contains("ALT"))) {
                    return now.plusDays(14); // 2 недели для пересдачи ALT
                } else {
                    return now.plusDays(30); // стандартный временный отвод
                }

            case PERMANENTLY_DEFERRED:
                return null; // нет следующей даты

            default:
                return now.plusDays(NORMAL_DONATION_INTERVAL_DAYS);
        }
    }

    private String formatBloodGroupInfo(Analysis analysis) {
        if (analysis.getBlood_group() != null && analysis.getRhesus_factor() != null) {
            return analysis.getBlood_group() + analysis.getRhesus_factor();
        }
        return "Не определена";
    }

    private boolean isPositive(String testResult) {
        return "POSITIVE".equalsIgnoreCase(testResult) ||
                "INCONCLUSIVE".equalsIgnoreCase(testResult) ||
                "PENDING".equalsIgnoreCase(testResult);
    }
}
