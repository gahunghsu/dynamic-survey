import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SurveyService } from '../../services/survey.service';
import { Survey, Question } from '../../models/survey.model';

/**
 * [教學說明] SurveyFillComponent (問卷填寫頁面)
 * -----------------------------------------------------------------------------
 * 1. 動態表單生成：根據後端傳回的題目型態，動態建立 FormGroup。
 * 2. 處理不同類型的輸入：Radio (單選), Checkbox (多選), Textarea (簡答)。
 */
@Component({
  selector: 'app-survey-fill',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatRadioModule,
    MatCheckboxModule,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './survey-fill.component.html',
  styleUrl: './survey-fill.component.scss'
})
export class SurveyFillComponent implements OnInit {
  private fb = inject(FormBuilder);
  private surveyService = inject(SurveyService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  survey = signal<Survey | null>(null);
  fillForm: FormGroup = this.fb.group({});

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSurvey(Number(id));
    }
  }

  loadSurvey(id: number) {
    this.surveyService.getSurveyById(id).subscribe({
      next: (data) => {
        this.survey.set(data);
        this.buildForm(data.questions);
      },
      error: () => this.snackBar.open('無法載入問卷', '關閉', { duration: 3000 })
    });
  }

  /**
   * [教學重點] 動態建立表單
   * 為每個題目建立一個對應的 FormControl。
   */
  private buildForm(questions: Question[]) {
    const group: any = {};
    questions.forEach(q => {
      if (q.type === 'MULTI') {
        // 多選題使用 FormArray 來存多個選項 ID
        group[q.id!] = this.fb.array([], q.required ? Validators.required : null);
      } else {
        // 單選與簡答使用一般的 FormControl
        group[q.id!] = ['', q.required ? Validators.required : null];
      }
    });
    this.fillForm = this.fb.group(group);
  }

  /**
   * [功能] 處理多選題的勾選
   */
  onCheckboxChange(questionId: number, optionId: number, checked: boolean) {
    const formArray = this.fillForm.get(questionId.toString()) as FormArray;
    if (checked) {
      formArray.push(this.fb.control(optionId));
    } else {
      const index = formArray.controls.findIndex(x => x.value === optionId);
      formArray.removeAt(index);
    }
  }

  onSubmit() {
    if (this.fillForm.valid) {
      const formValue = this.fillForm.value;
      const surveyData = this.survey()!;
      
      // 轉換成後端需要的 ResponseDTO 格式
      const submission = {
        surveyId: surveyData.id,
        answers: Object.keys(formValue).map(qId => {
          const val = formValue[qId];
          const question = surveyData.questions.find(q => q.id === Number(qId))!;
          
          return {
            questionId: Number(qId),
            optionIds: question.type === 'TEXT' ? [] : (Array.isArray(val) ? val : [val]),
            answerText: question.type === 'TEXT' ? val : null
          };
        })
      };

      this.surveyService.submitResponse(surveyData.id!, submission).subscribe({
        next: () => {
          this.snackBar.open('問卷提交成功，感謝您的參與！', '關閉', { duration: 3000 });
          this.router.navigate(['/home']);
        },
        error: () => this.snackBar.open('提交失敗，請檢查是否已登入', '關閉', { duration: 3000 })
      });
    }
  }
}
