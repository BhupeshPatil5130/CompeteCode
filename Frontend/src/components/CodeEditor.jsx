import Editor from '@monaco-editor/react';

const LANGUAGE_MAP = {
  java: 'java',
  python: 'python',
  python3: 'python',
  cpp: 'cpp',
  'c++': 'cpp',
  javascript: 'javascript',
  js: 'javascript',
};

export default function CodeEditor({ value, onChange, language = 'java', height = '100%', readOnly = false }) {
  const monacoLang = LANGUAGE_MAP[language.toLowerCase()] || 'java';

  return (
    <Editor
      height={height}
      language={monacoLang}
      value={value}
      onChange={(value) => onChange(value ?? '')}
      theme="vs-dark"
      options={{
        minimap: { enabled: false },
        fontSize: 14,
        fontFamily: 'JetBrains Mono, monospace',
        lineNumbers: 'on',
        scrollBeyondLastLine: false,
        automaticLayout: true,
        tabSize: 4,
        readOnly,
        padding: { top: 16 },
      }}
    />
  );
}
