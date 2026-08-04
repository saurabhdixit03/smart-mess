import { useState } from "react";

import { Copy, Check } from "lucide-react";

import Button from "../Button/Button";

interface CopyButtonProps {
  text: string;
}

export default function CopyButton({
  text,
}: CopyButtonProps) {
  const [copied, setCopied] =
    useState(false);

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(
        text
      );

      setCopied(true);

      setTimeout(() => {
        setCopied(false);
      }, 2000);

    } catch (error) {

      console.error(error);

    }
  }

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleCopy}
    >
      {copied ? (
        <>
          <Check size={16} />
          Copied
        </>
      ) : (
        <>
          <Copy size={16} />
          Copy
        </>
      )}
    </Button>
  );
}