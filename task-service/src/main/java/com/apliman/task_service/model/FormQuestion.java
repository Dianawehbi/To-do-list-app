// @Entity
// @Table(name = "form_questions", schema = "db_form")
// @Data
// public class FormQuestion {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne(optional = false, fetch = FetchType.LAZY)
//     @JsonIgnore
//     @JoinColumn(name = "form_id")
//     private Form form;

//     @Enumerated(EnumType.STRING)
//     @Column( name = "question_type")
//     private QuestionType questionType;

//     @Column(columnDefinition = "TEXT", name = "question_text")
//     private String questionText;

//     @Column( name = "required")
//     private Boolean required = false;

//     // Store question-specific config as JSON
//     @Column(columnDefinition = "JSON")
//     private String config;

//     @Column(name = "order_index")
//     private int orderIndex = 0;

//     @CreationTimestamp
//     @Column(name = "created_at", updatable = false)
//     private LocalDateTime createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;
// }
