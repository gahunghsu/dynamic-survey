import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { SurveyService } from '../../services/survey.service';

/**
 * [教學說明] UserHistoryComponent (個人填寫紀錄頁面)
 */
@Component({
  selector: 'app-user-history',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './user-history.component.html',
  styleUrl: './user-history.component.scss'
})
export class UserHistoryComponent implements OnInit {
  private surveyService = inject(SurveyService);
  
  history = signal<any[]>([]);
  displayedColumns: string[] = ['index', 'surveyTitle', 'submittedAt', 'actions'];

  ngOnInit() {
    this.surveyService.getUserHistory().subscribe({
      next: (data) => this.history.set(data),
      error: (err) => console.error('無法載入歷史紀錄', err)
    });
  }
}
