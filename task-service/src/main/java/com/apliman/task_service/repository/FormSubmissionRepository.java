// @Repository
// public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
//     // fetch all submissions by form ID
//     Page<FormSubmission> findByFormId(Long formId, Pageable pageable);
//     // count all submissions by form ID directly in DB
//     // int countByFormId(Long formId);
//     @Query("""
//                 SELECT COUNT(fs)
//                 FROM FormSubmission fs
//                 LEFT JOIN fs.formAccessToken t
//                 WHERE fs.form.id = :formId
//                 AND (t IS NULL OR t.isExtraToken = false)
//             """)
//     int countSubmissionsExcludingExtra(@Param("formId") Long formId);
//     // Optional<FormSubmission> findByFormAccessTokenId(Long tokenId);
//     // fetch all submissions for a specific token
//     List<FormSubmission> findByFormAccessTokenId(Long tokenId);
//     Optional<FormSubmission> findTopByFormAccessTokenIdOrderBySubmittedAtDesc(Long formAccessTokenId);
//     List<FormSubmission> findAllByFormAccessTokenIdOrderBySubmittedAtDesc(Long tokenId);
//     void deleteAllByFormAccessToken(FormAccessToken token);
//     int countByFormAccessToken(FormAccessToken token);
// }
