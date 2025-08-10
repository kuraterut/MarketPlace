{{- define "payment-service.name" -}}
payment-service
{{- end }}

{{- define "payment-service.fullname" -}}
{{ .Release.Name }}-{{ include "payment-service.name" . }}
{{- end }}
