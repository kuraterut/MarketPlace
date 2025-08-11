{{/*
Helpers for api-gateway chart
*/}}
{{- define "api-gateway.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "api-gateway.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name (include "api-gateway.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "api-gateway.labels" -}}
app.kubernetes.io/name: {{ include "api-gateway.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "api-gateway.selectorLabels" -}}
app.kubernetes.io/name: {{ include "api-gateway.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- /* provide eureka uri with fallback to global */ -}}
{{- define "api-gateway.eurekaUri" -}}
{{- if .Values.eureka.uri }}{{ .Values.eureka.uri }}{{- else if .Values.global.eureka.uri }}{{ .Values.global.eureka.uri }}{{- else }}http://eureka-server:8761/eureka{{- end -}}
{{- end }}

{{- /* provide auth url with fallback to global */ -}}
{{- define "api-gateway.authUrl" -}}
{{- if .Values.auth.url }}{{ .Values.auth.url }}{{- else if .Values.global.auth.url }}{{ .Values.global.auth.url }}{{- else }}http://auth-service:8080{{- end -}}
{{- end }}

{{- /* jwt secret name helper: existingSecret or default */ -}}
{{- define "api-gateway.jwtSecretName" -}}
{{- if .Values.auth.jwtSecret.existingSecret }}{{ .Values.auth.jwtSecret.existingSecret }}{{- else }}{{ include "api-gateway.fullname" . }}-jwt{{- end -}}
{{- end }}
