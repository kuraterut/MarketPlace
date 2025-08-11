{{- /*
Return the chart name
*/ -}}
{{- define "product-service.name" -}}
product-service
{{- end -}}

{{- /*
Return the full name of the release (например, имя Helm релиза + chart name)
*/ -}}
{{- define "product-service.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "product-service.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- /*
Return the chart version
*/ -}}
{{- define "product-service.chart" -}}
{{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{- /*
Generate labels common for all resources
*/ -}}
{{- define "product-service.labels" -}}
app.kubernetes.io/name: {{ include "product-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- /*
Helper for postgres secret name
*/ -}}
{{- define "product-service.postgresSecretName" -}}
postgres-product-secret
{{- end -}}

{{- /*
Helper for keydb secret name
*/ -}}
{{- define "product-service.keydbSecretName" -}}
keydb-product-secret
{{- end -}}

{{- /*
Helper for container image name with tag
*/ -}}
{{- define "product-service.image" -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end -}}

{{- /*
Helper for kafka bootstrap server string.
Priority:
  1) .Values.global.kafka.bootstrapServers
  2) .Values.kafka.bootstrapServers
  3) Construct from release name: <release>-kafka-headless.<namespace>.svc.cluster.local:9092
*/ -}}
{{- define "product-service.kafkaBootstrap" -}}
{{- if .Values.global.kafka.bootstrapServers }}
{{- .Values.global.kafka.bootstrapServers -}}
{{- else if .Values.kafka.bootstrapServers }}
{{- .Values.kafka.bootstrapServers -}}
{{- else }}
{{- printf "%s-kafka-headless.%s.svc.cluster.local:9092" .Release.Name .Release.Namespace -}}
{{- end -}}
{{- end -}}
