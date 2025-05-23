package com.jamjam.bookjeok.domains.question.command.service;

import com.jamjam.bookjeok.common.service.FileStorageService;

import com.jamjam.bookjeok.domains.question.command.dto.request.QuestionRequest;
import com.jamjam.bookjeok.domains.question.command.dto.response.QuestionResponse;
import com.jamjam.bookjeok.domains.question.command.entity.Question;
import com.jamjam.bookjeok.domains.question.command.entity.QuestionAnswer;
import com.jamjam.bookjeok.domains.question.command.repository.QuestionAnswerRepository;
import com.jamjam.bookjeok.domains.question.command.repository.QuestionRepository;
import com.jamjam.bookjeok.exception.question.InconsistentQuestionException;
import com.jamjam.bookjeok.exception.question.NotFoundQuestionException;
import com.jamjam.bookjeok.exception.question.QuestionErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionMemberCommandServiceImpl implements QuestionMemberCommandService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final QuestionRepository questionRepository;
    private final FileStorageService fileStorageService;

    @Value("${image.image-url}")
    private String IMAGE_URL;

    @Override
    public QuestionResponse registQuestion(QuestionRequest request, MultipartFile questionImg) {

        String replaceFileName = fileStorageService.storeFile(questionImg);

        Question question = Question.builder()
                .questionCategoriesId(request.questionCategoryId())
                .writerUid(request.writerUid())
                .title(request.title())
                .contents(request.contents())
                .questionsImgUrl(replaceFileName)
                .build();

        Question newQuestion = questionRepository.save(question);

        return buildQuestionResponse(newQuestion);
    }

    @Override
    public void validReceivedAnswer(Long questionId) {

        Optional<List<QuestionAnswer>> findAnswers = questionAnswerRepository.findByQuestionId(questionId);

        if(!findAnswers.get().isEmpty()) {
            throw new InconsistentQuestionException(QuestionErrorCode.RECEIVED_ANSWER_QUESTION);
        }

    }

    @Override
    public QuestionResponse modifyQuestion(Long questionId, QuestionRequest request, MultipartFile questionImg) {

        Optional<Question> findQuestion = questionRepository.findByQuestionId(questionId);

        if(findQuestion.isEmpty()) {
            throw new NotFoundQuestionException(QuestionErrorCode.NOTFOUND_QUESTION);
        }

        if(questionImg != null) {
            String replaceFileName = fileStorageService.storeFile(questionImg);
            if(findQuestion.get().getQuestionsImgUrl() != null){
                String oldFileName = findQuestion.get().getQuestionsImgUrl().replace(IMAGE_URL,"");
                fileStorageService.deleteFile(oldFileName);
            }
            findQuestion.get().changeImageUrl(replaceFileName);
        }

        findQuestion.get().updateQuestion(
                request.title(),
                request.contents(),
                LocalDateTime.now()
        );

        Question modQuestion = questionRepository.save(findQuestion.get());

        return buildQuestionResponse(modQuestion);

    }

    @Override
    public QuestionResponse deleteQuestion(Long questionId) {
        Optional<Question> findQuestion = questionRepository.findByQuestionId(questionId);

        if(findQuestion.isEmpty()) {
            throw new NotFoundQuestionException(QuestionErrorCode.NOTFOUND_QUESTION);
        }

        findQuestion.get().deleteQuestion(
                LocalDateTime.now(),
                true
        );

        Question delQuestion = questionRepository.save(findQuestion.get());

        return buildQuestionResponse(delQuestion);
    }


    private QuestionResponse buildQuestionResponse(Question question) {

        return QuestionResponse.builder()
                .questionId(question.getQuestionId())
                .questionCategoryId(question.getQuestionCategoriesId())
                .writerUid(question.getWriterUid())
                .title(question.getTitle())
                .contents(question.getContents())
                .createdAt(question.getCreatedAt())
                .modifiedAt(question.getModifiedAt())
                .isDeleted(question.isDeleted())
                .build();
    }
}
