const map = {
  EASY: 'badge-easy',
  MEDIUM: 'badge-medium',
  HARD: 'badge-hard',
  EXPERT: 'badge-expert',
};

export default function DifficultyBadge({ difficulty }) {
  return (
    <span className={map[difficulty] || 'badge-medium'}>
      {difficulty}
    </span>
  );
}
