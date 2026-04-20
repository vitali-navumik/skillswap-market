import { useNavigate } from "react-router-dom";

interface BackButtonProps {
  fallbackTo?: string;
  testId?: string;
}

export function BackButton({ fallbackTo = "/offers", testId = "back-button" }: BackButtonProps) {
  const navigate = useNavigate();

  const onClick = () => {
    if (window.history.length > 1) {
      navigate(-1);
      return;
    }
    navigate(fallbackTo);
  };

  return (
    <button
      type="button"
      className="back-button"
      data-testid={testId}
      aria-label="Go back"
      title="Go back"
      onClick={onClick}
    >
      <span aria-hidden="true">←</span>
    </button>
  );
}
