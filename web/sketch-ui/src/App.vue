<script setup lang="ts">
import { ref, onMounted } from "vue";
import _ from "lodash";

const keywords = [
  ["fr", "From", "408729 / SGH W9 B10"],
  ["to", "To", "408729 / SKGH AnE"],
  ["at", "Time", "1430h"],

  ["pt", "Patient name", "John Smith"],
  ["nric", "NRIC last 4", "123A"],
  ["wt", "Weight", "50kg"],
  ["cp", "Complaint", "chest pain"],

  ["btyp", "Bill type", "B(ill)/C(ash)"],
  ["bto", "Bill to", "NTFGH"],
  ["pr", "Price", "108.00"],

  ["ct", "Contact", "Iris Koh"],
  ["tel", "Contact phone", "81234567"],
  ["rem", "Remarks"],
];

const message = ref<Record<string, string>>({
  fr: "",
  to: "",
  at: "",
});

const craftMessage = (msg: Record<string, string>): string => {
  return keywords
    .map(([short, long]) => {
      console.log(msg[short], short);
      if (short in msg) {
        return `${short} ${msg[short]}`;
      }
    })
    .filter(Boolean)
    .join("\n");
};

const parse = (e: FocusEvent): void => {
  const lines = ((e.target as any).value as string).split("\n");
  const result = {} as Record<string, string>;
  const unknownLines = [] as string[];

  lines.forEach((line: string) => {
    const matchingKeyword = keywords.find(([short]) =>
      line.toLowerCase().startsWith(short + " ")
    );
    if (matchingKeyword) {
      result[matchingKeyword[0]] = line.substring(
        matchingKeyword[0].length + 1
      );
    } else if (line.trim()) {
      unknownLines.push(line);
    }
  });

  if (unknownLines.length > 0) {
    result.rem = [result.rem || "", unknownLines].filter(Boolean).join(" ; ");
  }

  message.value = result;
  input.value = craftMessage(result);
};

const addToMessage = (s: string[]) => {
  s.forEach((k) => (message.value[k] ||= ""));
  input.value = craftMessage(message.value);
};

// Hints code
const input = ref(craftMessage(message.value));

const hintElemRef = ref<null | HTMLElement>(null);

function syncHintScroll(h: any) {
  const elem = hintElemRef.value;

  if (elem) {
    elem.scrollTop = h.target.scrollTop;
    elem.scrollLeft = h.target.scrollLeft;
  }
}

const handleInput = (e: InputEvent) => {
  syncHintScroll(e);

  const t = (e.target as HTMLTextAreaElement).value;
  input.value = t;
};

type Token = {
  type: "error" | "key" | "value";
  content: string;
};
type HintedLine = {
  tokens: Token[];
  hint: string | null;
};

const makeHints = (s: string): HintedLine[] => {
  const lines = s.split("\n");
  const matches = lines.map((line) => line.match(/^(\w+)(.*)$/));

  return matches.map((match, index) => {
    if (match) {
      const key = match[1];
      let matchingKeyword = keywords.find((s) => s[0] === key.toLowerCase());

      if (matchingKeyword) {
        const tokens: Token[] = [
          { content: match[1], type: "key" },
          { content: match[2], type: "value" },
        ];

        const hints = [
          `(${matchingKeyword[1]})`,
          !match[2].trim() && matchingKeyword[2],
        ].filter(Boolean);

        return {
          tokens,
          hint: hints.join(" "),
        };
      } else {
        return {
          tokens: [
            { content: match[1], type: "error" },
            { content: match[2], type: "value" },
          ],
          hint: `Unknown key ${match[1]}`,
        };
      }
    } else if (index === matches.length - 1) {
      const keywordHints = _.difference(
        keywords.map((s) => s[0]),
        matches.map((s) => s && s[1].toLowerCase())
      ).join("/");
      return {
        tokens: [{ content: lines[index], type: "error" }],
        hint: `(${keywordHints})`,
      };
    } else {
      return {
        tokens: [{ content: lines[index], type: "error" }],
        hint: "",
      };
    }
  });
};
</script>

<template>
  <div class="flexy">
    <div style="display: flex; flex-direction: horizontal; flex: 0 1 auto">
      <button
        class="flat-button"
        style="flex: 1 1 0"
        @click="addToMessage(['fr', 'to', 'at'])"
      >
        Trip
      </button>
      <button
        class="flat-button"
        style="flex: 1 1 0"
        @click="addToMessage(['pt', 'nric', 'wt', 'cp'])"
      >
        Caller / Patient
      </button>
      <button
        class="flat-button"
        style="flex: 1 1 0"
        @click="addToMessage(['btyp', 'bto', 'pr'])"
      >
        Billing
      </button>
    </div>
    <div class="message-part">
      <textarea
        @blur="parse"
        @input="handleInput"
        @scroll.passive="syncHintScroll"
        :value="input"
        style="resize: none"
      ></textarea>
      <div class="hint" ref="hintElemRef">
        <template v-for="(h, i) in makeHints(input)" :key="`${i}-br`">
          <span
            v-for="(token, j) in h.tokens"
            :class="{ ['token-' + token.type]: true }"
            :key="`${i}-${j}`"
          >
            {{ token.content }} </span
          ><span v-if="h.hint" class="line-hint"> {{ h.hint }} </span><br />
        </template>
      </div>
    </div>
    <button>Dummy clickable</button>
  </div>
</template>

<style scoped>
.flat-button {
  background-color: #9cf;
  margin: 0.25em;
}
.flexy {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  gap: 10px;
}

.message-part {
  position: relative;
  flex: 1 0 150px;
  min-height: 150px;
}

.message-part .hint,
.message-part textarea {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  font-family: monospace;
  font-size: 20px;
  line-height: 1.5;
  z-index: 2;
  background-color: transparent;
  overflow-x: hidden;
  overflow-y: scroll;
}

.message-part textarea {
  color: #000;
}

.message-part .hint {
  border: solid 1px #600;
  white-space: pre-wrap;
  text-align: left;
  margin: 0px;
  padding: 2px;
  pointer-events: none;
  user-select: none;
  color: red;
  z-index: 3;
}

.token-error {
  color: #f00;
}

.token-key {
  color: #090;
}

.token-value {
  color: #666;
}
.line-hint {
  position: absolute;
  background-color: #ddd;
  color: #666;
  font-size: 65%;
  padding: 3px;
  border-radius: 3px;
  margin-left: 3em;
}
</style>
