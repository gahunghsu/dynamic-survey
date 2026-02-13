import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { SurveyService } from '../../../services/survey.service';
import { SurveyStats, QuestionStats } from '../../../models/survey-stats.model';

/**
 * [教學說明] SurveyStatsComponent (問卷統計頁面)
 * -----------------------------------------------------------------------------
 * 展示了：
 * 1. 數據聚合顯示：總填寫人數。
 * 2. 圖表視覺化：使用 ng2-charts 將選擇題數據轉換為圓餅圖。
 * 3. 列表呈現：顯示簡答題的所有文字回答。
 */
@Component({
  selector: 'app-survey-stats',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    RouterLink,
    BaseChartDirective
  ],
  templateUrl: './survey-stats.component.html',
  styleUrl: './survey-stats.component.scss'
})
export class SurveyStatsComponent implements OnInit {
  private surveyService = inject(SurveyService);
  private route = inject(ActivatedRoute);

  stats = signal<SurveyStats | null>(null);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.surveyService.getSurveyStats(Number(id)).subscribe({
        next: (data) => this.stats.set(data),
        error: (err) => console.error('無法載入統計數據', err)
      });
    }
  }

  /**
   * [教學重點] 準備圖表資料
   * 將後端的統計 Map 轉換為 Chart.js 所需的格式。
   */
  getChartData(q: QuestionStats): ChartData<'pie'> {
    if (!q.optionStats) return { labels: [], datasets: [] };

    const labels = Object.values(q.optionStats).map(o => o.optionText);
    const data = Object.values(q.optionStats).map(o => o.count);

    return {
      labels: labels,
      datasets: [
        {
          data: data,
          backgroundColor: [
            '#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'
          ]
        }
      ]
    };
  }

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      }
    }
  };
}
