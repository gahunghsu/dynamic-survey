import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DatePickerModule } from 'primeng/datepicker';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectButtonModule } from 'primeng/selectbutton';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { DividerModule } from 'primeng/divider';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SurveyService } from '../../../services/survey.service';
import { Question, Survey, Option } from '../../../models/survey.model';

@Component({
  selector: 'app-survey-editor',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink, ButtonModule,
    InputTextModule, TextareaModule, DatePickerModule, CheckboxModule,
    SelectButtonModule, ToastModule, DividerModule
  ],
  providers: [MessageService],
  templateUrl: './survey-editor.component.html',
  styleUrl: './survey-editor.component.scss'
})
export class SurveyEditorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private surveyService = inject(SurveyService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  // --- 流程狀態 ---
  surveyId = signal<number | null>(null);
  activeStep = signal(0); // 0: 基本資料, 1: 題目設定, 2: 預覽確認
  
  surveyForm: FormGroup;

  questionTypes = [
    { label: '單選', value: 'SINGLE', icon: 'pi pi-circle' },
    { label: '多選', value: 'MULTI', icon: 'pi pi-check-square' },
    { label: '文字', value: 'TEXT', icon: 'pi pi-align-left' }
  ];

  constructor() {
    // 建立結構化表單
    this.surveyForm = this.fb.group({
      id: [null],
      title: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(300)]],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      questions: this.fb.array([]) // 題目 FormArray
    });
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.surveyId.set(Number(id));
      this.loadSurvey(this.surveyId()!);
    } else {
      this.addQuestion(); // 預設新增一題
    }
  }

  // --- FormArray Getters ---
  get questionsArray(): FormArray {
    return this.surveyForm.get('questions') as FormArray;
  }

  getOptionsArray(qIndex: number): FormArray {
    return this.questionsArray.at(qIndex).get('options') as FormArray;
  }

  // --- 題目操作方法 ---
  addQuestion() {
    const qGroup = this.fb.group({
      id: [null],
      title: ['', Validators.required],
      type: ['SINGLE', Validators.required],
      required: [true],
      options: this.fb.array([]) // 選項 FormArray
    });
    this.addOption(qGroup.get('options') as FormArray); // 預設給兩個選項
    this.addOption(qGroup.get('options') as FormArray);
    this.questionsArray.push(qGroup);
  }

  removeQuestion(index: number) {
    this.questionsArray.removeAt(index);
  }

  addOption(optionsArray: FormArray) {
    optionsArray.push(this.fb.group({
      id: [null],
      optionText: ['', Validators.required]
    }));
  }

  removeOption(qIndex: number, oIndex: number) {
    this.getOptionsArray(qIndex).removeAt(oIndex);
  }

  onTypeChange(qIndex: number) {
    const qGroup = this.questionsArray.at(qIndex);
    const options = this.getOptionsArray(qIndex);
    if (qGroup.get('type')?.value === 'TEXT') {
      options.clear();
    } else if (options.length === 0) {
      this.addOption(options);
      this.addOption(options);
    }
  }

  // --- 流程控制 ---

  /**
   * 跳轉至確認頁 (將表單資料打包存入 Session)
   */
  goToConfirm() {
    if (this.surveyForm.invalid) {
      this.messageService.add({ severity: 'warn', summary: '提醒', detail: '請填寫完整資訊' });
      return;
    }

    // 將 FormArray 的資料整理成 DTO 格式
    const formValue = this.surveyForm.value;
    const surveyData: Survey = {
      ...formValue,
      status: 'DRAFT',
      questions: formValue.questions.map((q: any, i: number) => ({
        ...q,
        orderIndex: i,
        options: q.options.map((o: any, j: number) => ({ ...o, orderIndex: j }))
      }))
    };

    this.surveyService.saveAdminSurveyToSession(surveyData).subscribe(() => {
      this.activeStep.set(2);
      window.scrollTo(0, 0);
    });
  }

  onFinalSubmit(isPublish: boolean) {
    if (!confirm(`確定要${isPublish ? '儲存並發佈' : '僅儲存'}嗎？`)) return;

    this.surveyService.confirmAdminSubmit(isPublish).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: '成功', detail: '問卷處理完成' });
        setTimeout(() => this.router.navigate(['/admin']), 1000);
      },
      error: (err) => this.messageService.add({ severity: 'error', summary: '失敗', detail: err.error?.message || '儲存失敗' })
    });
  }

  // --- API ---
  loadSurvey(id: number) {
    this.surveyService.getAdminSurveyById(id).subscribe(s => {
      this.surveyForm.patchValue({
        id: s.id,
        title: s.title,
        description: s.description,
        startDate: new Date(s.startDate),
        endDate: new Date(s.endDate)
      });

      this.questionsArray.clear();
      s.questions.forEach(q => {
        const qGroup = this.fb.group({
          id: [q.id],
          title: [q.title, Validators.required],
          type: [q.type, Validators.required],
          required: [q.required],
          options: this.fb.array(q.options.map(o => this.fb.group({
            id: [o.id],
            optionText: [o.optionText, Validators.required]
          })))
        });
        this.questionsArray.push(qGroup);
      });
    });
  }
}
