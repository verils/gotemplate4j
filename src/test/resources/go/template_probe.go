package main

import (
	"bufio"
	"bytes"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"os"
	"strings"
	"text/template"
)

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1024), 1024*1024)

	for scanner.Scan() {
		parts := strings.SplitN(scanner.Text(), "\t", 2)
		if len(parts) != 2 {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte("invalid request")))
			continue
		}

		templateBytes, err := base64.StdEncoding.DecodeString(parts[0])
		if err != nil {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte(err.Error())))
			continue
		}
		dataBytes, err := base64.StdEncoding.DecodeString(parts[1])
		if err != nil {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte(err.Error())))
			continue
		}

		var data interface{}
		if err := json.Unmarshal(dataBytes, &data); err != nil {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte(err.Error())))
			continue
		}

		parsed, err := template.New("differential").Parse(string(templateBytes))
		if err != nil {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte(err.Error())))
			continue
		}

		var output bytes.Buffer
		if err := parsed.Execute(&output, data); err != nil {
			fmt.Println("E\t" + base64.StdEncoding.EncodeToString([]byte(err.Error())))
			continue
		}
		fmt.Println("O\t" + base64.StdEncoding.EncodeToString(output.Bytes()))
	}
}
