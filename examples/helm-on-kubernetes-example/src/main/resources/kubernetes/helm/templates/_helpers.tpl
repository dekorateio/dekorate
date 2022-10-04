{{/*
Expand the name of the chart.
*/}}
{{- define "foo.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 5 }}
{{- end }}