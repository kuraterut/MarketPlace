{{- define "kafka.name" -}}
kafka
{{- end }}

{{- define "kafka.fullname" -}}
{{ printf "%s-%s" .Release.Name (include "kafka.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "kafka.serviceName" -}}
{{ include "kafka.fullname" . }}
{{- end }}
