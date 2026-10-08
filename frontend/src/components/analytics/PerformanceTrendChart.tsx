import React from 'react';
import { TrendingUp, Calendar } from 'lucide-react';
import { PerformanceTrend } from '../../types';

interface PerformanceTrendChartProps {
  trend: PerformanceTrend[];
}

export const PerformanceTrendChart: React.FC<PerformanceTrendChartProps> = ({ trend }) => {
  if (!trend || trend.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 text-center text-slate-400 text-sm">
        No performance history recorded yet. Complete interviews to plot your score trajectory.
      </div>
    );
  }

  // SVG dimensions & padding
  const width = 800;
  const height = 260;
  const padLeft = 40;
  const padRight = 30;
  const padTop = 30;
  const padBottom = 40;

  const chartW = width - padLeft - padRight;
  const chartH = height - padTop - padBottom;

  const maxScore = 100;
  const minScore = 0;

  const points = trend.map((item, idx) => {
    const x =
      trend.length === 1
        ? padLeft + chartW / 2
        : padLeft + (idx / (trend.length - 1)) * chartW;
    const y = padTop + chartH - ((item.score - minScore) / (maxScore - minScore)) * chartH;
    return { x, y, ...item };
  });

  const pathD =
    points.length === 1
      ? `M ${points[0].x} ${points[0].y}`
      : points.reduce(
          (acc, p, idx) => (idx === 0 ? `M ${p.x} ${p.y}` : `${acc} L ${p.x} ${p.y}`),
          ''
        );

  const areaD =
    points.length === 1
      ? ''
      : `${pathD} L ${points[points.length - 1].x} ${padTop + chartH} L ${points[0].x} ${
          padTop + chartH
        } Z`;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <TrendingUp className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-white font-semibold text-base">Performance Score Trajectory</h3>
            <p className="text-xs text-slate-400">Chronological score progression over completed sessions</p>
          </div>
        </div>

        <div className="flex items-center gap-3 text-xs text-slate-400 font-mono">
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-cyan-400" />
            <span>Score (/100)</span>
          </span>
          <span>•</span>
          <span>{trend.length} Recorded Sessions</span>
        </div>
      </div>

      {/* SVG Responsive Container */}
      <div className="w-full overflow-x-auto">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto min-w-[500px]"
          style={{ overflow: 'visible' }}
        >
          <defs>
            <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#06b6d4" stopOpacity="0.3" />
              <stop offset="100%" stopColor="#06b6d4" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Horizontal Grid lines */}
          {[0, 25, 50, 75, 100].map((val) => {
            const y = padTop + chartH - ((val - minScore) / (maxScore - minScore)) * chartH;
            return (
              <g key={val}>
                <line
                  x1={padLeft}
                  y1={y}
                  x2={width - padRight}
                  y2={y}
                  stroke="#1e293b"
                  strokeDasharray="4 4"
                />
                <text
                  x={padLeft - 8}
                  y={y + 4}
                  fill="#64748b"
                  fontSize="10"
                  fontFamily="monospace"
                  textAnchor="end"
                >
                  {val}
                </text>
              </g>
            );
          })}

          {/* Shaded Area Under Curve */}
          {areaD && <path d={areaD} fill="url(#trendGradient)" />}

          {/* Line Path */}
          <path
            d={pathD}
            fill="none"
            stroke="#06b6d4"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Data Points */}
          {points.map((p, idx) => (
            <g key={idx} className="group cursor-pointer">
              {/* Outer halo */}
              <circle
                cx={p.x}
                cy={p.y}
                r="6"
                fill="#0f172a"
                stroke="#06b6d4"
                strokeWidth="2.5"
              />
              <circle
                cx={p.x}
                cy={p.y}
                r="3"
                fill="#38bdf8"
              />

              {/* Value bubble above point */}
              <text
                x={p.x}
                y={p.y - 12}
                fill="#e2e8f0"
                fontSize="11"
                fontWeight="bold"
                fontFamily="monospace"
                textAnchor="middle"
              >
                {p.score}
              </text>

              {/* Date label under bottom axis */}
              <text
                x={p.x}
                y={height - padBottom + 20}
                fill="#64748b"
                fontSize="10"
                fontFamily="monospace"
                textAnchor="middle"
              >
                {p.date.slice(5)}
              </text>
            </g>
          ))}
        </svg>
      </div>
    </div>
  );
};
