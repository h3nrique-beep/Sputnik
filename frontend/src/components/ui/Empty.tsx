export function Empty({ text }: { text: string }) {
  return (
    <div className="empty-state" role="status">
      <p>{text}</p>
    </div>
  );
}
