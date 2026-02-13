import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// PrimeNG Modules
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DatePickerModule } from 'primeng/datepicker';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectButtonModule } from 'primeng/selectbutton';
import { CardModule } from 'primeng/card';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { TooltipModule } from 'primeng/tooltip';
import { MessageModule } from 'primeng/message';
import { FluidModule } from 'primeng/fluid';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SurveyService } from '../../../services/survey.service';
import { Question, Survey, Option } from '../../../models/survey.model';

@Component({
  selector: 'app-survey-editor',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    DatePickerModule,
    CheckboxModule,
    SelectButtonModule,
    CardModule,
    IconFieldModule,
    InputIconModule,
    TooltipModule,
    MessageModule,
    FluidModule,
    DividerModule,
    ToastModule
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

  // --- State (Signals) ---
  surveyId = signal<number | null>(null);
  activeTab = signal(0); // 0: 基本資料, 1: 題目設定
  selectedQuestionIndex = signal(0);

  surveyForm: FormGroup;

  // Question Type Options for SelectButton
  questionTypes = [
    { label: '單選題', value: 'SINGLE', icon: 'pi pi-circle' },
    { label: '多選題', value: 'MULTI', icon: 'pi pi-check-square' },
    { label: '簡答題', value: 'TEXT', icon: 'pi pi-align-left' }
  ];

  constructor() {
    this.surveyForm = this.fb.group({
      id: [null],
      title: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(300)]],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required],
      status: ['DRAFT', Validators.required],
      questions: this.fb.array([])
    });
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.surveyId.set(Number(id));
      this.loadSurvey(this.surveyId()!);
    } else {
      this.addQuestion();
    }
  }

  // --- Getters ---
  get questionsArray(): FormArray {
    return this.surveyForm.get('questions') as FormArray;
  }

  getOptionsArray(questionIndex: number): FormArray {
    return this.questionsArray.at(questionIndex).get('options') as FormArray;
  }

  // --- Navigation Methods ---
  goToStep(step: number) {
    if (step === 1 && this.isBasicInfoInvalid()) {
      this.messageService.add({
        severity: 'warn',
        summary: '資料未完全',
        detail: '請先填寫問卷基本資料'
      });
      return;
    }
    this.activeTab.set(step);
  }

  isBasicInfoInvalid(): boolean {
    const { title, startDate, endDate } = this.surveyForm.controls;
    return title.invalid || startDate.invalid || endDate.invalid;
  }

  // --- Question Logic ---
  addQuestion() {
    const questionGroup = this.fb.group({
      id: [null],
      title: ['', [Validators.required, Validators.maxLength(75)]],
      type: ['SINGLE', Validators.required],
      required: [true],
      orderIndex: [this.questionsArray.length],
      options: this.fb.array([])
    });

    this.addOption(questionGroup.get('options') as FormArray);
    this.addOption(questionGroup.get('options') as FormArray);

    this.questionsArray.push(questionGroup);
    this.selectedQuestionIndex.set(this.questionsArray.length - 1);
  }

  removeQuestion(index: number) {
    this.questionsArray.removeAt(index);
    if (this.selectedQuestionIndex() >= this.questionsArray.length) {
      this.selectedQuestionIndex.set(Math.max(0, this.questionsArray.length - 1));
    }
  }

  addOption(optionsArray: FormArray) {
    const optionGroup = this.fb.group({
      id: [null],
      optionText: ['', Validators.required],
      orderIndex: [optionsArray.length]
    });
    optionsArray.push(optionGroup);
  }

  removeOption(questionIndex: number, optionIndex: number) {
    this.getOptionsArray(questionIndex).removeAt(optionIndex);
  }

  onTypeChange(index: number) {
    const questionGroup = this.questionsArray.at(index);
    const type = questionGroup.get('type')?.value;
    const optionsArray = questionGroup.get('options') as FormArray;

    if (type === 'TEXT') {
      optionsArray.clear();
    } else if (optionsArray.length === 0) {
      this.addOption(optionsArray);
      this.addOption(optionsArray);
    }
  }

  selectQuestion(index: number) {
    this.selectedQuestionIndex.set(index);
  }

  // --- API Methods ---
  loadSurvey(id: number) {
    this.surveyService.getSurveyById(id).subscribe({
      next: (survey) => {
        this.surveyForm.patchValue({
          id: survey.id,
          title: survey.title,
          description: survey.description,
          startDate: survey.startDate ? new Date(survey.startDate) : null,
          endDate: survey.endDate ? new Date(survey.endDate) : null,
          status: survey.status
        });

        this.questionsArray.clear();
        survey.questions.forEach(q => {
          const qGroup = this.fb.group({
            id: [q.id],
            title: [q.title, [Validators.required, Validators.maxLength(75)]],
            type: [q.type, Validators.required],
            required: [q.required],
            orderIndex: [q.orderIndex],
            options: this.fb.array([])
          });

          const optionsArray = qGroup.get('options') as FormArray;
          q.options.forEach(o => {
            optionsArray.push(this.fb.group({
              id: [o.id],
              optionText: [o.optionText, Validators.required],
              orderIndex: [o.orderIndex]
            }));
          });

          this.questionsArray.push(qGroup);
        });
      },
      error: () => this.messageService.add({ severity: 'error', summary: '失敗', detail: '無法載入問卷' })
    });
  }

  onSubmit() {
    if (this.surveyForm.valid) {
      const formValue = this.surveyForm.value;

      formValue.questions.forEach((q: any, i: number) => {
        q.orderIndex = i;
        q.options.forEach((o: any, j: number) => o.orderIndex = j);
      });

      this.surveyService.saveSurvey(formValue).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: '成功', detail: '問卷已儲存' });
          setTimeout(() => this.router.navigate(['/admin']), 1000);
        },
        error: () => this.messageService.add({ severity: 'error', summary: '失敗', detail: '儲存失敗' })
      });
    } else {
      this.messageService.add({ severity: 'error', summary: '錯誤', detail: '請檢查輸入欄位' });
    }
  }
}
