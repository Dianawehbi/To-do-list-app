// package com.apliman.formsystem.service;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.apliman.formsystem.DTO.form.FormRequestDTO;
// import com.apliman.formsystem.DTO.question.config.MultiChoiceConfigDTO;
// import com.apliman.formsystem.DTO.question.config.QuestionConfigDTO;
// import com.apliman.formsystem.DTO.question.config.RatingConfigDTO;
// import com.apliman.formsystem.DTO.question.config.SingleChoiceConfigDTO;
// import com.apliman.formsystem.DTO.question.config.TextConfigDTO;
// import com.apliman.formsystem.mapper.QuestionMapper;
// import com.apliman.formsystem.model.Form;
// import com.apliman.formsystem.model.FormQuestion;

// @Service
// public class ValidatorService {

//     @Autowired
//     private QuestionMapper questionMapper;

//     public void validateForm(Form form) {
//         if (form == null)
//             throw new IllegalArgumentException("Form is required");

//         if (form.getName() == null || form.getName().isBlank())
//             throw new IllegalArgumentException("Missing parameter: Form name");

//         if (form.getCompanyName() == null || form.getName().isBlank())
//             throw new IllegalArgumentException("Missing parameter: Company Name");

//         // If form is Expiry, start and end date are required
//         validateDates(form.getIsExpiry(), form.getStartDate(), form.getEndDate());

//         // questions must exist
//         if (form.getQuestions() == null || form.getQuestions().isEmpty())
//             throw new IllegalArgumentException(
//                     "Form must contain at least one question");
//         form.getQuestions().forEach(question -> validateQuestion(question));
//     }

//     public void validateFormforUpdate(FormRequestDTO dto) {
//         if (dto == null)
//             throw new IllegalArgumentException("Form data is required");

//         // If name is provided, it must not be blank
//         if (dto.getName() != null && dto.getName().isBlank())
//             throw new IllegalArgumentException("Form name cannot be null");

//         // If isLimited , startDate, and endDate are provided, validate them
//         if (dto.getIsExpiry() != null) {
//             validateDates(dto.getIsExpiry(), dto.getStartDate(), dto.getEndDate());
//         }

//         // If questions provided → validate structure
//         if (dto.getQuestions() != null) {

//             List<FormQuestion> questions = dto.getQuestions().stream()
//                     .map(questionMapper::toEntity)
//                     .toList();

//             questions.forEach(this::validateQuestion);
//         }
//     }

//     public void validateQuestion(FormQuestion question) {

//         if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
//             throw new IllegalArgumentException("Question text is required");
//         }

//         if (question.getQuestionType() == null) {
//             throw new IllegalArgumentException("Question type is required");
//         }

//         if (question.getConfig() == null) {
//             throw new IllegalArgumentException("Config is required");
//         }

//         QuestionConfigDTO config = questionMapper.mapStringToConfig(question.getConfig(),
//                 question.getQuestionType().name());
//         if (config instanceof TextConfigDTO textConfig) {
//             validateText(textConfig);
//         } else if (config instanceof RatingConfigDTO ratingConfig) {
//             validateRating(ratingConfig);
//         } else if (config instanceof SingleChoiceConfigDTO singleChoiceConfig) {
//             validateSingleChoice(singleChoiceConfig);
//         } else if (config instanceof MultiChoiceConfigDTO multiChoiceConfig) {
//             validateMultiChoice(multiChoiceConfig);
//         } else {
//             throw new IllegalArgumentException("Unsupported question config type");
//         }
//     }

//     private void validateText(TextConfigDTO config) {
//         // if (config.getMaxLength() != null && config.getMaxLength() <= 0) {
//         // throw new IllegalArgumentException("maxLength must be greater than 0");
//         // }
//     }

//     private void validateRating(RatingConfigDTO config) {

//         if (config.getMin() == null || config.getMax() == null) {
//             throw new IllegalArgumentException("Rating min and max are required");
//         }

//         if (config.getMin() >= config.getMax()) {
//             throw new IllegalArgumentException("Rating min must be less than max");
//         }
//     }

//     private void validateSingleChoice(SingleChoiceConfigDTO config) {

//         if (config.getOptions() == null || config.getOptions().isEmpty()) {
//             throw new IllegalArgumentException("Options are required for SINGLE_CHOICE");
//         }

//         // if (config.getOptions().size() != 1) {
//         // throw new IllegalArgumentException("SINGLE_CHOICE should contain at least one
//         // option");
//         // }
//     }

//     private void validateMultiChoice(MultiChoiceConfigDTO config) {

//         if (config.getOptions() == null || config.getOptions().isEmpty()) {
//             throw new IllegalArgumentException("Options are required for MULTI_CHOICE");
//         }
//     }

//     public void validateDates(boolean isExpiry, LocalDateTime startdate, LocalDateTime endDate) {
//         // If form is limited, start and end date are required
//         if (Boolean.TRUE.equals(isExpiry)) {
//             if (startdate == null || endDate == null) {
//                 throw new IllegalArgumentException(
//                         "StartDate and EndDate are required when the form is Expiry");
//             }
//             if (startdate.isAfter(endDate)) {
//                 throw new IllegalArgumentException(
//                         "StartDate cannot be after EndDate");
//             }
//         } else {
//             // start date is required
//             if (startdate == null) {
//                 throw new IllegalArgumentException(
//                         "StartDate is required");
//             }
//         }
//     }

//     public void validateIdentifier(String identifier) {

//         boolean isEmail = identifier.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
//         boolean isPhone = identifier.matches("^\\+?[0-9]{7,15}$");

//         if (!isEmail && !isPhone) {
//             throw new IllegalArgumentException(
//                     "Identifier must be a valid email or phone number");
//         }
//     }
// }
