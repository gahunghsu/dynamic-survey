import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { SurveyService } from '../../../services/survey.service';
import { Survey } from '../../../models/survey.model';

/**
 * [教學說明] SurveyListComponent (後台問卷列表)
 * -----------------------------------------------------------------------------
 * 展示了：
 * 1. MatTable: 呈現結構化資料。
 * 2. 資料串接: 在 ngOnInit 中呼叫 Service 取得資料。
 * 3. 狀態顯示: 使用 MatChips 顯示問卷是 DRAFT 還是 PUBLISHED。
 */
@Component({
  selector: 'app-survey-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './survey-list.component.html',
  styleUrl: './survey-list.component.scss'
})
export class SurveyListComponent implements OnInit {
  private surveyService = inject(SurveyService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  // 使用 Signal 管理問卷列表
  surveys = signal<Survey[]>([]);
  
  // 定義表格要顯示的欄位
  displayedColumns: string[] = ['id', 'title', 'status', 'period', 'actions'];

  ngOnInit() {
    this.loadSurveys();
  }

  loadSurveys() {
    this.surveyService.getAllSurveys().subscribe({
      next: (data) => this.surveys.set(data),
      error: () => this.snackBar.open('無法載入問卷列表', '關閉', { duration: 3000 })
    });
  }

  onEdit(survey: Survey) {
    this.router.navigate(['/admin/edit', survey.id]);
  }

  onDelete(id: number) {
    if (confirm('確定要刪除這份問卷嗎？此動作無法復原。')) {
      this.surveyService.deleteSurvey(id).subscribe({
        next: () => {
          this.snackBar.open('問卷已刪除', '關閉', { duration: 3000 });
          this.loadSurveys(); // 重新載入列表
        },
        error: () => this.snackBar.open('刪除失敗', '關閉', { duration: 3000 })
      });
    }
  }
}
