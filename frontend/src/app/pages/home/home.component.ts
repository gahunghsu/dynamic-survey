import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { SurveyService } from '../../services/survey.service';
import { Survey } from '../../models/survey.model';

/**
 * [教學說明] HomeComponent (前台首頁)
 * -----------------------------------------------------------------------------
 * 展示可供填寫的問卷列表。
 */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  private surveyService = inject(SurveyService);
  
  surveys = signal<Survey[]>([]);

  ngOnInit() {
    this.surveyService.getActiveSurveys().subscribe({
      next: (data) => this.surveys.set(data),
      error: (err) => console.error('無法載入問卷', err)
    });
  }
}
