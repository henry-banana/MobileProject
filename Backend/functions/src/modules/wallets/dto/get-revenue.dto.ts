import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional } from 'class-validator';

export enum RevenuePeriod {
  TODAY = 'today',
  WEEK = 'week',
  MONTH = 'month',
  YEAR = 'year',
  ALL = 'all',
}

export class GetRevenueDto {
  @ApiPropertyOptional({
    enum: RevenuePeriod,
    example: RevenuePeriod.MONTH,
    description: 'Time period for revenue calculation',
    default: RevenuePeriod.MONTH,
  })
  @IsOptional()
  @IsEnum(RevenuePeriod)
  period?: RevenuePeriod = RevenuePeriod.MONTH;
}

export class DailyRevenueDto {
  @ApiProperty({ example: '2026-01-29', description: 'Date in YYYY-MM-DD format' })
  date: string;

  @ApiProperty({ example: 125000, description: 'Total revenue for this day' })
  amount: number;

  @ApiProperty({ example: 5, description: 'Number of orders completed' })
  orderCount: number;
}

export class RevenueStatsDto {
  @ApiProperty({ example: 55000, description: 'Revenue today' })
  today: number;

  @ApiProperty({ example: 350000, description: 'Revenue this week (Mon-Sun)' })
  week: number;

  @ApiProperty({ example: 1200000, description: 'Revenue this month' })
  month: number;

  @ApiProperty({ example: 2500000, description: 'Revenue this year' })
  year: number;

  @ApiProperty({ example: 2500000, description: 'Total revenue all time' })
  all: number;

  @ApiProperty({ type: [DailyRevenueDto], description: 'Daily breakdown (last 7/30 days)' })
  dailyBreakdown: DailyRevenueDto[];

  @ApiProperty({ example: '2026-01-29T10:00:00Z', description: 'Calculation timestamp' })
  calculatedAt: string;
}
