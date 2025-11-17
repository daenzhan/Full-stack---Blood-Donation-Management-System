package org.example.analysisservice;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private static final double MIN_HEMOGLOBIN = 12.5;
    private static final double MAX_HEMOGLOBIN = 16.0;
    private static final double MAX_ALT_LEVEL = 40.0;
    private static final int DEFERRAL_DAYS_LOW_HEMOGLOBIN = 30;
    private static final int NORMAL_DONATION_INTERVAL_DAYS = 60;

    public DonorRecommendation generateRecommendations(Analysis analysis) {
        List<String> rejectionReasons = new ArrayList<>();
        List<String> healthRecommendations = new ArrayList<>();
        List<String> dietaryRecommendations = new ArrayList<>();

        // Проверка инфекционных заболеваний
        checkInfectiousDiseases(analysis, rejectionReasons);

        // Детальная проверка показателей с конкретными рекомендациями
        checkHemoglobinDetailed(analysis, rejectionReasons, healthRecommendations, dietaryRecommendations);
        checkAltLevelDetailed(analysis, rejectionReasons, healthRecommendations, dietaryRecommendations);
        checkBloodTypeDetailed(analysis, dietaryRecommendations);

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
                .analysisDate(analysis.getAnalysis_date() != null ? analysis.getAnalysis_date() : LocalDateTime.now())
                .build();
    }

    private void checkInfectiousDiseases(Analysis analysis, List<String> rejectionReasons) {
        if (isPositive(analysis.getHiv())) {
            rejectionReasons.add("<i class='fas fa-times-circle text-danger'></i> Обнаружена ВИЧ-инфекция - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getHepatitisB())) {
            rejectionReasons.add("<i class='fas fa-times-circle text-danger'></i> Обнаружен гепатит B - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getHepatitisC())) {
            rejectionReasons.add("<i class='fas fa-times-circle text-danger'></i> Обнаружен гепатит C - постоянное отстранение от донорства");
        }

        if (isPositive(analysis.getSyphilis())) {
            rejectionReasons.add("<i class='fas fa-exclamation-triangle text-warning'></i> Обнаружен сифилис - отстранение до полного излечения");
        }

        if (isPositive(analysis.getBrucellosis())) {
            rejectionReasons.add("<i class='fas fa-exclamation-triangle text-warning'></i> Обнаружена бруцеллезная инфекция - отстранение на 6 месяцев");
        }
    }

    private void checkHemoglobinDetailed(Analysis analysis, List<String> rejectionReasons,
                                         List<String> healthRecommendations, List<String> dietaryRecommendations) {
        if (analysis.getHemoglobin() != null) {
            double hemoglobin = analysis.getHemoglobin();

            if (hemoglobin < MIN_HEMOGLOBIN) {
                String message = String.format("<i class='fas fa-arrow-down text-danger'></i> Низкий уровень гемоглобина: %.1f г/дл (норма: %.1f-%.1f г/дл)",
                        hemoglobin, MIN_HEMOGLOBIN, MAX_HEMOGLOBIN);
                rejectionReasons.add(message);

                healthRecommendations.add("<i class='fas fa-tint text-info'></i> У вас пониженный гемоглобин. Это может вызывать усталость и слабость");
                healthRecommendations.add("<i class='fas fa-pills text-primary'></i> Рекомендуется проконсультироваться с терапевтом о возможном приеме железосодержащих препаратов");

                dietaryRecommendations.add("<i class='fas fa-utensils text-success'></i> ДЛЯ ПОВЫШЕНИЯ ГЕМОГЛОБИНА УПОТРЕБЛЯЙТЕ:");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Красное мясо (говядина, баранина) - 3-4 раза в неделю");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Печень (говяжья, куриная) - 2 раза в неделю");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Шпинат, брокколи, свеклу - ежедневно");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Чечевицу, фасоль, нут - 4-5 раз в неделю");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Гранаты, яблоки, хурму - ежедневно");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Гречневую кашу - 3-4 раза в неделю");
                dietaryRecommendations.add("<i class='fas fa-lightbulb text-warning'></i> СОВЕТ: Употребляйте железосодержащие продукты с витамином C для лучшего усвоения");

            } else if (hemoglobin > MAX_HEMOGLOBIN) {
                healthRecommendations.add("<i class='fas fa-arrow-up text-warning'></i> У вас повышенный гемоглобин. Пейте больше воды - не менее 2.5 литров в день");
                healthRecommendations.add("<i class='fas fa-tint text-primary'></i> Ограничьте употребление железосодержащих продуктов до нормализации показателей");

            } else {
                healthRecommendations.add("<i class='fas fa-check-circle text-success'></i> Уровень гемоглобина в норме! Продолжайте поддерживать сбалансированное питание");
                dietaryRecommendations.add("<i class='fas fa-utensils text-success'></i> ДЛЯ ПОДДЕРЖАНИЯ ГЕМОГЛОБИНА:");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Употребляйте железосодержащие продукты 2-3 раза в неделю");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Включайте в рацион овощи и фрукты, богатые витамином C");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Пейте достаточное количество воды");
            }

            if (hemoglobin >= MIN_HEMOGLOBIN && hemoglobin < MIN_HEMOGLOBIN + 0.5) {
                healthRecommendations.add("<i class='fas fa-lightbulb text-warning'></i> Уровень гемоглобина близок к нижней границе нормы. Увеличьте потребление железосодержащих продуктов");
            }
        }
    }

    private void checkAltLevelDetailed(Analysis analysis, List<String> rejectionReasons,
                                       List<String> healthRecommendations, List<String> dietaryRecommendations) {
        if (analysis.getAlt_level() != null) {
            double altLevel = analysis.getAlt_level();

            if (altLevel > MAX_ALT_LEVEL) {
                String message = String.format("<i class='fas fa-exclamation-triangle text-warning'></i> Повышенный уровень ALT: %.1f Ед/л (норма до %.1f Ед/л)",
                        altLevel, MAX_ALT_LEVEL);
                healthRecommendations.add(message);

                if (altLevel > MAX_ALT_LEVEL * 2) {
                    rejectionReasons.add("<i class='fas fa-times-circle text-danger'></i> Значительно повышен уровень ALT - требуется обследование у гепатолога");
                    healthRecommendations.add("<i class='fas fa-exclamation-circle text-danger'></i> Сильно повышен уровень печеночных ферментов! Срочно обратитесь к врачу");
                } else {
                    healthRecommendations.add("<i class='fas fa-search text-info'></i> Умеренно повышен уровень печеночных ферментов. Рекомендуется консультация терапевта");
                }

                dietaryRecommendations.add("<i class='fas fa-ban text-danger'></i> ДЛЯ НОРМАЛИЗАЦИИ ПЕЧЕНИ ИСКЛЮЧИТЕ:");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-danger'></i> Алкогольные напитки - полностью на 4-6 недель");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-danger'></i> Жареную и жирную пищу");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-danger'></i> Фастфуд и полуфабрикаты");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-danger'></i> Сладкие газированные напитки");

                dietaryRecommendations.add("<i class='fas fa-leaf text-success'></i> ДЛЯ ВОССТАНОВЛЕНИЯ ПЕЧЕНИ УПОТРЕБЛЯЙТЕ:");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Овощи (артишоки, брокколи, цветная капуста)");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Зеленые листовые овощи");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Авокадо и оливковое масло");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Грецкие орехи и миндаль");

            } else {
                healthRecommendations.add("<i class='fas fa-check-circle text-success'></i> Уровень печеночных ферментов (ALT) в норме! Печень функционирует хорошо");
                dietaryRecommendations.add("<i class='fas fa-leaf text-success'></i> ДЛЯ ПОДДЕРЖАНИЯ ЗДОРОВЬЯ ПЕЧЕНИ:");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Продолжайте придерживаться сбалансированного питания");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Ограничивайте употребление алкоголя");
                dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Включайте в рацион овощи и полезные жиры");
            }
        }
    }

    private void checkBloodTypeDetailed(Analysis analysis, List<String> dietaryRecommendations) {
        if (analysis.getBlood_group() != null) {
            String bloodType = analysis.getBlood_group();
            String rhesus = analysis.getRhesus_factor();

            dietaryRecommendations.add("<i class='fas fa-tint text-danger'></i> РЕКОМЕНДАЦИИ ПО ПИТАНИЮ ДЛЯ ГРУППЫ КРОВИ " + bloodType + rhesus + ":");

            switch (bloodType) {
                case "O":
                    dietaryRecommendations.add("<i class='fas fa-drumstick-bite text-success'></i> ОСНОВА РАЦИОНА: Высокобелковые продукты");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Красное мясо, рыба, птица");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Овощи (брокколи, шпинат, морская капуста)");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Оливковое масло, грецкие орехи");
                    dietaryRecommendations.add("<i class='fas fa-times-circle text-danger'></i> ОГРАНИЧИТЬ: Молочные продукты, пшеницу, бобовые, кукурузу");
                    dietaryRecommendations.add("<i class='fas fa-running text-primary'></i> ФИЗИЧЕСКАЯ АКТИВНОСТЬ: Интенсивные тренировки, бег, плавание");
                    break;

                case "A":
                    dietaryRecommendations.add("<i class='fas fa-leaf text-success'></i> ОСНОВА РАЦИОНА: Вегетарианское питание");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Овощи (все виды, особенно зеленые)");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Тофу, соевые продукты, бобовые");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Крупы (гречка, овсянка, рис)");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Фрукты (ананасы, абрикосы, лимоны)");
                    dietaryRecommendations.add("<i class='fas fa-times-circle text-danger'></i> ОГРАНИЧИТЬ: Красное мясо, молочные продукты, пшеницу");
                    dietaryRecommendations.add("<i class='fas fa-spa text-primary'></i> ФИЗИЧЕСКАЯ АКТИВНОСТЬ: Йога, пилатес, ходьба, плавание");
                    break;

                case "B":
                    dietaryRecommendations.add("<i class='fas fa-balance-scale text-success'></i> ОСНОВА РАЦИОНА: Сбалансированное питание");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Мясо (кроме курицы), рыба");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Молочные продукты, яйца");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Крупы (овес, рис, просо)");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Овощи (зеленые, капуста, морковь)");
                    dietaryRecommendations.add("<i class='fas fa-times-circle text-danger'></i> ОГРАНИЧИТЬ: Курицу, гречку, кукурузу, арахис");
                    dietaryRecommendations.add("<i class='fas fa-table-tennis text-primary'></i> ФИЗИЧЕСКАЯ АКТИВНОСТЬ: Теннис, велоспорт, плавание, ходьба");
                    break;

                case "AB":
                    dietaryRecommendations.add("<i class='fas fa-fish text-success'></i> ОСНОВА РАЦИОНА: Смешанное питание");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Морепродукты, рыба, тофу");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Молочные продукты, яйца");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Овощи (все виды, особенно зеленые)");
                    dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Фрукты (вишня, виноград, киви)");
                    dietaryRecommendations.add("<i class='fas fa-times-circle text-danger'></i> ОГРАНИЧИТЬ: Красное мясо, кукурузу, гречку, фасоль");
                    dietaryRecommendations.add("<i class='fas fa-swimmer text-primary'></i> ФИЗИЧЕСКАЯ АКТИВНОСТЬ: Плавание, йога, танцы, велоспорт");
                    break;
            }
        }
    }

    private void addGeneralRecommendations(List<String> healthRecommendations,
                                           List<String> dietaryRecommendations) {
        healthRecommendations.add("<i class='fas fa-tint text-primary'></i> ОБЩИЕ РЕКОМЕНДАЦИИ ПО ЗДОРОВЬЮ:");
        healthRecommendations.add("<i class='fas fa-dot-circle text-primary'></i> Пейте 2-2.5 литра воды в день");
        healthRecommendations.add("<i class='fas fa-dot-circle text-primary'></i> Спите 7-8 часов в сутки");
        healthRecommendations.add("<i class='fas fa-dot-circle text-primary'></i> Избегайте алкоголя за 48 часов до донации");
        healthRecommendations.add("<i class='fas fa-dot-circle text-primary'></i> Регулярно занимайтесь физической активностью");

        dietaryRecommendations.add("<i class='fas fa-utensils text-success'></i> ОБЩИЕ ПИТАТЕЛЬНЫЕ РЕКОМЕНДАЦИИ:");
        dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Питайтесь сбалансированно 3-4 раза в день");
        dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Включайте в рацион свежие овощи и фрукты");
        dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Ограничивайте потребление сахара и соли");
        dietaryRecommendations.add("<i class='fas fa-dot-circle text-success'></i> Отдавайте предпочтение цельнозерновым продуктам");
    }

    // Остальные методы остаются без изменений...
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
                } else {
                    return now.plusDays(30);
                }
            case PERMANENTLY_DEFERRED:
                return null;
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
        return testResult != null && (
                "POSITIVE".equalsIgnoreCase(testResult) ||
                        "INCONCLUSIVE".equalsIgnoreCase(testResult) ||
                        "PENDING".equalsIgnoreCase(testResult) ||
                        "РЕАКТИВНЫЙ".equalsIgnoreCase(testResult) ||
                        "ПОЛОЖИТЕЛЬНЫЙ".equalsIgnoreCase(testResult)
        );
    }

    // Метод для базовых рекомендаций после донации
    public DonorRecommendation getGeneralDonationRecommendations(Long donorId) {
        return DonorRecommendation.builder()
                .donorId(donorId)
                .eligibilityStatus(EligibilityStatus.ELIGIBLE)
                .nextDonationDate(LocalDateTime.now().plusDays(60))
                .healthRecommendations(List.of(
                        "ОБЩИЕ РЕКОМЕНДАЦИИ ПОСЛЕ ДОНАЦИИ:",
                        "Отдохните 10-15 минут после донации",
                        "Не снимайте повязку в течение 4-5 часов",
                        "Избегайте тяжелых физических нагрузок 24 часа",
                        "Пейте много жидкости в течение 48 часов",
                        "При головокружении лягте и поднимите ноги",
                        "Не курите 2 часа после донации",
                        "Избегайте сауны и горячих ванн в день донации"
                ))
                .dietaryRecommendations(List.of(
                        "ПИТАНИЕ ДЛЯ ВОССТАНОВЛЕНИЯ:",
                        "Употребляйте богатую железом пищу: красное мясо, шпинат, бобовые",
                        "Ешьте продукты с витамином C для лучшего усвоения железа",
                        "Пейте не менее 2-3 литров воды в день",
                        "Избегайте алкоголя в течение 24 часов после донации",
                        "Включите в рацион орехи, сухофрукты и цельнозерновые продукты",
                        "Употребляйте белковые продукты для восстановления",
                        "Ешьте небольшими порциями 4-5 раз в день"
                ))
                .rejectionReasons(List.of())
                .bloodGroupInfo("Общие рекомендации после донации")
                .analysisDate(LocalDateTime.now())
                .build();
    }
}