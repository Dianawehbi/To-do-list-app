
// package com.apliman.formsystem.mapper;

// import org.mapstruct.Mapper;
// import org.mapstruct.Mapping;
// import org.mapstruct.Named;

// import com.apliman.formsystem.DTO.question.QuestionRequestDTO;
// import com.apliman.formsystem.DTO.question.QuestionResponseDTO;
// import com.apliman.formsystem.DTO.question.config.QuestionConfigDTO;
// import com.apliman.formsystem.model.FormQuestion;
// import com.apliman.formsystem.model.FormTemplateQuestion;
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;

// @Mapper(componentModel = "spring")
// public interface QuestionMapper {

//     ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;

//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "form", ignore = true)
//     @Mapping(target = "createdAt", ignore = true)
//     @Mapping(target = "updatedAt", ignore = true)
//     @Mapping(target = "questionType", source = "type")
//     @Mapping(target = "config", qualifiedByName = "mapConfigToString")
//     FormQuestion toEntity(QuestionRequestDTO dto);

//     @Mapping(target = "type", source = "questionType")
//     @Mapping(target = "config", expression = "java(mapStringToConfig(question.getConfig(), question.getQuestionType().name()))")
//     QuestionResponseDTO toDTO(FormQuestion question);

//     @Mapping(target = "config", expression = "java(mapStringToConfig(question.getConfig(), question.getQuestionType().name()))")
//     // @Mapping(target = "config", qualifiedByName = "mapStringToConfig")
//     @Mapping(target = "type", source = "questionType")
//     QuestionResponseDTO questionTemplatetoDTO(FormTemplateQuestion question);

//     // ---------------- Helper methods ----------------

//     @Named("mapConfigToString")
//     default String mapConfigToString(QuestionConfigDTO dto) {
//         if (dto == null)
//             return null;
//         try {
//             return objectMapper.writeValueAsString(dto);
//         } catch (Exception e) {
//             throw new RuntimeException("Error converting QuestionConfigDTO to JSON string in Question Mapper", e);
//         }
//     }

//     // @Named("mapStringToConfig")
//     // default QuestionConfigDTO mapStringToConfig(String config) {
//     //     System.out.println("Inside mao string to config ");
//     //     if (config == null)
//     //         return null;
//     //     try {
//     //         System.out.println(config);
//     //         return objectMapper.readValue(config, QuestionConfigDTO.class);
//     //     } catch (Exception e) {
//     //         System.out.println("Errorrrr");
//     //         System.out.println(e);
//     //         throw new RuntimeException("Error converting JSON string to Map in Question Mapper", e);
//     //     }
//     // }

//     @Named("mapStringToConfig")
//     default QuestionConfigDTO mapStringToConfig(String config, String type) {

//         if (config == null)
//             return null;

//         try {

//             // inject type into the JSON object
//             String jsonWithType = "{ \"type\": \"" + type + "\", " + config.substring(1);

//             return objectMapper.readValue(jsonWithType, QuestionConfigDTO.class);

//         } catch (Exception e) {
//             System.out.println(e);
//             throw new RuntimeException("Error converting JSON string to QuestionConfigDTO", e);
//         }
//     }
// }


