import { Icon } from "@/components/ui/icon";

type SelectFieldProps = { label: string; value: string; options: readonly string[]; onChange: (value: string) => void };

export function SelectField({ label, value, options, onChange }: SelectFieldProps) {
  return <label className="field"><span>{label}</span><div className="select-wrap"><select value={value} onChange={(event) => onChange(event.target.value)}><option value="">Select {label.toLowerCase()}</option>{options.map((option) => <option key={option}>{option}</option>)}</select><Icon name="chevron" /></div></label>;
}
