import { Icon } from "@/components/ui/icon";

type SelectOption = string | { value: string; label: string };
type SelectFieldProps = { label: string; value: string; options: readonly SelectOption[]; onChange: (value: string) => void };

export function SelectField({ label, value, options, onChange }: SelectFieldProps) {
  return <label className="field"><span>{label}</span><div className="select-wrap"><select value={value} onChange={(event) => onChange(event.target.value)}><option value="">Select {label.toLowerCase()}</option>{options.map((option) => typeof option === "string" ? <option key={option} value={option}>{option}</option> : <option key={option.value} value={option.value}>{option.label}</option>)}</select><Icon name="chevron" /></div></label>;
}
