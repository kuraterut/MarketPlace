{{- define "kafka.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end }}



{{- define "kafka.serviceName" -}}
{{ include "kafka.name" . }}
{{- end }}
