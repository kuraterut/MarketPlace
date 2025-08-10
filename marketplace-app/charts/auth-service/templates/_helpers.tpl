{{/* ---------------------------------------------------------------------------
   helpers for auth-service chart
   - все define должны иметь префикс "auth-service." чтобы избежать коллизий
   --------------------------------------------------------------------------- */}}

{{- /*
Return chart name (or override)
*/ -}}
{{- define "auth-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- /*
Return chart full name: <release>-<name> (truncated to 63 chars)
Allow override by .Values.fullnameOverride
*/ -}}
{{- define "auth-service.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name (include "auth-service.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- /*
Return chart@version - handy for labels
*/ -}}
{{- define "auth-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version -}}
{{- end -}}

{{- /*
Common labels for all resources in this chart.
Use | nindent/Nindent/indent when rendering.
*/ -}}
{{- define "auth-service.labels" -}}
app.kubernetes.io/name: {{ include "auth-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ include "auth-service.chart" . }}
{{- end -}}

{{- /*
Labels used in selector (keeps them consistent)
*/ -}}
{{- define "auth-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "auth-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- /*
Secret name for Postgres credentials.
*/ -}}
{{- define "auth-service.postgresSecretName" -}}
{{ include "auth-service.fullname" . }}-postgres
{{- end -}}

{{/*
Имя секрета для KeyDB
*/}}
{{- define "auth-service.keydbSecretName" -}}
{{- if .Values.keydb.existingSecret }}
{{ .Values.keydb.existingSecret }}
{{- else }}
{{ include "auth-service.fullname" . }}-keydb
{{- end }}
{{- end }}


{{- /*
Helper for kafka bootstrap server string.
Priority:
  1) .Values.global.kafka.bootstrapServers
  2) .Values.kafka.bootstrapServers
  3) Construct from release name: <release>-kafka-headless.<namespace>.svc.cluster.local:9092
*/ -}}
{{- define "auth-service.kafkaBootstrap" -}}
{{- if .Values.global.kafka.bootstrapServers }}
{{- .Values.global.kafka.bootstrapServers -}}
{{- else if .Values.kafka.bootstrapServers }}
{{- .Values.kafka.bootstrapServers -}}
{{- else }}
{{- printf "%s-kafka-headless.%s.svc.cluster.local:9092" .Release.Name .Release.Namespace -}}
{{- end -}}
{{- end -}}
