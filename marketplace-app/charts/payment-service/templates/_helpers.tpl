{{- define "payment-service.name" -}}
payment-service
{{- end }}

{{- define "payment-service.fullname" -}}
{{ .Release.Name }}-{{ include "payment-service.name" . }}
{{- end }}

{{- /*
Helper for kafka bootstrap server string.
Priority:
  1) .Values.global.kafka.bootstrapServers
  2) .Values.kafka.bootstrapServers
  3) Construct from release name: <release>-kafka-headless.<namespace>.svc.cluster.local:9092
*/ -}}
{{- define "payment-service.kafkaBootstrap" -}}
{{- if .Values.global.kafka.bootstrapServers }}
{{- .Values.global.kafka.bootstrapServers -}}
{{- else if .Values.kafka.bootstrapServers }}
{{- .Values.kafka.bootstrapServers -}}
{{- else }}
{{- printf "%s-kafka-headless.%s.svc.cluster.local:9092" .Release.Name .Release.Namespace -}}
{{- end -}}
{{- end -}}

{{- define "payment-service.labels" -}}
app.kubernetes.io/name: {{ include "payment-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}
