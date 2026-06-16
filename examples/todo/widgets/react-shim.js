// Map bare `react` imports to the framework's React (exposed on window.SpringReact),
// so this bundle shares ONE React instance with SpringReact's runtime — no duplicate
// React, no "invalid hook call". esbuild aliases `react` to this file.
const React = window.SpringReact.React
export default React
export const {
  useState,
  useEffect,
  useRef,
  useMemo,
  useCallback,
  createElement,
  Fragment,
} = React
