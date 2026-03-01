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
import { MatIconModule } from '@angular/material/icon'; // [修正] 加入此行
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; // [修正] 加入此行
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SurveyService } from '../../services/survey.service';
import { Survey, Question } from '../../models/survey.model';

@Component({
  selector: 'app-survey-fill',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatRadioModule, MatCheckboxModule,
    MatSnackBarModule, MatIconModule, MatProgressSpinnerModule, RouterLink // [修正] 加入模組
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
  
  // 狀態控制
  isConfirmPage = signal(false); // 是否處於確認頁狀態
  previewData = signal<any>(null); // 暫存從 Session 拿回來的資料供顯示

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

  private buildForm(questions: Question[]) {
    // 1. 建立固定資訊欄位 (對應後端 DTO)
    const group: any = {
      name: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9-]{10,15}$/)]],
      email: ['', [Validators.required, Validators.email]],
      age: [null] // 選填
    };

    // 2. 建立動態題目欄位
    questions.forEach(q => {
      if (q.type === 'MULTI') {
        group[q.id!] = this.fb.array([], q.required ? Validators.required : null);
      } else {
        group[q.id!] = ['', q.required ? Validators.required : null];
      }
    });
    this.fillForm = this.fb.group(group);
  }

  onCheckboxChange(questionId: number, optionId: number, checked: boolean) {
    const formArray = this.fillForm.get(questionId.toString()) as FormArray;
    if (checked) {
      formArray.push(this.fb.control(optionId));
    } else {
      const index = formArray.controls.findIndex(x => x.value === optionId);
      formArray.removeAt(index);
    }
  }

  /**
   * 第一步：點擊「下一步」，將資料存入 Session
   */
  onGoToConfirm() {
    if (this.fillForm.invalid) {
      this.snackBar.open('請填寫所有必填欄位', '關閉', { duration: 3000 });
      return;
    }

    const formValue = this.fillForm.value;
    const submission = this.formatSubmission(formValue);

    this.surveyService.saveToSession(submission).subscribe({
      next: (res) => {
        if (res.code === 200) {
          // 暫存成功，切換至確認模式
          this.isConfirmPage.set(true);
          this.previewData.set(submission);
          window.scrollTo(0, 0);
        } else {
          this.snackBar.open(res.message, '關閉', { duration: 3000 });
        }
      },
      error: (err) => this.snackBar.open(err.error?.message || '傳送失敗', '關閉', { duration: 3000 })
    });
  }

  /**
   * 第二步：在確認頁點擊「確認提交」
   */
  onFinalSubmit() {
    if (!confirm('確定要送出問卷嗎？送出後將無法修改。')) return;

    this.surveyService.confirmSubmit().subscribe({
      next: (res) => {
        if (res.code === 200) {
          this.snackBar.open('問卷提交成功！', '關閉', { duration: 3000 });
          this.router.navigate(['/home']);
        } else {
          this.snackBar.open(res.message, '關閉', { duration: 3000 });
        }
      },
      error: () => this.snackBar.open('提交失敗，請稍後再試', '關閉', { duration: 3000 })
    });
  }

  // 格式化資料為後端 DTO 格式
  private formatSubmission(formValue: any) {
    const surveyData = this.survey()!;
    return {
      surveyId: surveyData.id,
      name: formValue.name,
      phone: formValue.phone,
      email: formValue.email,
      age: formValue.age,
      answers: Object.keys(formValue)
        .filter(key => !['name', 'phone', 'email', 'age'].includes(key))
        .map(qId => {
          const val = formValue[qId];
          const question = surveyData.questions.find(q => q.id === Number(qId))!;
          return {
            questionId: Number(qId),
            optionIds: question.type === 'TEXT' ? [] : (Array.isArray(val) ? val : [val]),
            answerText: question.type === 'TEXT' ? val : null
          };
        })
    };
  }

  /**
   * 取得選項文字 (預覽用)
   */
  getOptionText(qId: number, oId: any): string {
    const q = this.survey()?.questions.find(x => x.id === qId);
    return q?.options.find(o => o.id === Number(oId))?.optionText || oId;
  }
}
