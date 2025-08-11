{{- define "order-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "order-service.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name (include "order-service.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "order-service.labels" -}}
app.kubernetes.io/name: {{ include "order-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "order-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "order-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- /*
Helper for kafka bootstrap server string.
Priority:
  1) .Values.global.kafka.bootstrapServers
  2) .Values.kafka.bootstrapServers
  3) Construct from release name: <release>-kafka-headless.<namespace>.svc.cluster.local:9092
*/ -}}
{{- define "order-service.kafkaBootstrap" -}}
{{- if .Values.global.kafka.bootstrapServers }}
{{- .Values.global.kafka.bootstrapServers -}}
{{- else if .Values.kafka.bootstrapServers }}
{{- .Values.kafka.bootstrapServers -}}
{{- else }}
{{- printf "%s-kafka-headless.%s.svc.cluster.local:9092" .Release.Name .Release.Namespace -}}
{{- end -}}
{{- end -}}